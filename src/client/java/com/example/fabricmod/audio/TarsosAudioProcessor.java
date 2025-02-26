package com.example.fabricmod.audio;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.HammingWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TarsosAudioProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TarsosAudioProcessor.class);
    
    private AudioDispatcher dispatcher;
    private Thread audioThread;
    
    // 音频参数
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 2048;
    private static final int OVERLAP = 1024;
    
    // 音量和频谱相关
    private final AtomicReference<Float> currentVolume = new AtomicReference<>(0.0f);
    private final AtomicReference<float[]> currentSpectrum = new AtomicReference<>(new float[BUFFER_SIZE/2]);
    private final AtomicReference<Float> currentPitch = new AtomicReference<>(0.0f);
    private final AtomicReference<Float> currentDB = new AtomicReference<>(-60.0f);

    private float dbMin = -80.0f;  // 默认值
    private static final float DB_MAX = 0.0f;
    
    public void setDBMin(float sensitivity) {
        // 将敏感度（0-100）映射到分贝值（-80-0）
        this.dbMin = -(80.0f * (sensitivity / 100.0f));
    }
    
    public void start(Mixer.Info mixerInfo) {
        stop();
        
        try {
            if (mixerInfo == null) {
                throw new IllegalStateException("未指定音频设备");
            }
            
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            LOGGER.info("尝试启动音频设备: {}", mixer.getMixerInfo().getName());
            
            // 确保是输入设备
            Line.Info targetDataLineInfo = new Line.Info(TargetDataLine.class);
            if (!mixer.isLineSupported(targetDataLineInfo)) {
                throw new IllegalStateException("选择的设备不是输入设备");
            }
            
            // 获取输入线路
            TargetDataLine line = (TargetDataLine) mixer.getLine(targetDataLineInfo);
            line.open();
            line.start();
            
            AudioInputStream audioStream = new AudioInputStream(line);
            JVMAudioInputStream audioInputStream = new JVMAudioInputStream(audioStream);
            
            // 使用设备的实际格式
            AudioFormat format = line.getFormat();
            LOGGER.info("使用音频格式: {}Hz, {}bit, {} 声道", 
                       format.getSampleRate(),
                       format.getSampleSizeInBits(),
                       format.getChannels());
            
            dispatcher = new AudioDispatcher(audioInputStream, BUFFER_SIZE, OVERLAP);
            setupAudioProcessors();
            
            audioThread = new Thread(dispatcher, "Audio Processing Thread");
            audioThread.start();

            LOGGER.info("成功启动音频处理");
            
        } catch (Exception e) {
            LOGGER.error("启动音频处理失败: {}", e.getMessage());
            throw new RuntimeException("无法启动音频处理: " + e.getMessage(), e);
        }
    }

    private void setupAudioProcessors() {
        // 添加音高检测处理器
        PitchProcessor pitchProcessor = new PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN, 
            SAMPLE_RATE, 
            BUFFER_SIZE,
            (result, event) -> {
                if (result.getPitch() != -1) {
                    currentPitch.set(result.getPitch());
                }
            }
        );
        dispatcher.addAudioProcessor(pitchProcessor);

        // 添加音量和频谱处理器
        dispatcher.addAudioProcessor(new AudioProcessor() {
            private final FFT fft = new FFT(BUFFER_SIZE, new HammingWindow());
            private final float[] amplitudes = new float[BUFFER_SIZE / 2];
            private static final float NOISE_THRESHOLD = 0.005f; // 添加噪声阈值

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioData = audioEvent.getFloatBuffer();
                
                // 计算音量（dB）
                float rms = (float) audioEvent.getRMS();
                float db = (float)(20 * Math.log10(rms));
                currentDB.set(db);
                currentVolume.set(normalizeDB(db));

                // 计算频谱
                float[] transformBuffer = new float[BUFFER_SIZE];
                System.arraycopy(audioData, 0, transformBuffer, 0, audioData.length);
                fft.forwardTransform(transformBuffer);
                fft.modulus(transformBuffer, amplitudes);

                currentSpectrum.set(amplitudes);
                return true;
            }

            @Override
            public void processingFinished() {
            }
        });
    }

    private float normalizeDB(float db) {
        return (Math.max(dbMin, Math.min(DB_MAX, db)) - dbMin) / (DB_MAX - dbMin);
    }

    public float getCurrentVolume() {
        return currentVolume.get();
    }

    public float getCurrentDB() {
        return currentDB.get();
    }

    public float getCurrentPitch() {
        return currentPitch.get();
    }

    public float[] getSpectrum() {
        return currentSpectrum.get().clone();
    }

    public void stop() {
        if (dispatcher != null) {
            dispatcher.stop();
            dispatcher = null;
        }
        if (audioThread != null) {
            audioThread.interrupt();
            audioThread = null;
        }
        currentVolume.set(0.0f);
        currentDB.set(-60.0f);
        currentPitch.set(0.0f);
    }

    public static List<AudioDevice> getAvailableInputs() {
        List<AudioDevice> devices = new ArrayList<>();
        int index = 0;
        
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            try {
                Mixer mixer = AudioSystem.getMixer(info);
                Line.Info[] lineInfos = mixer.getTargetLineInfo();
                
                // 检查是否支持 TargetDataLine（输入设备）
                for (Line.Info lineInfo : lineInfos) {
                    if (lineInfo.getLineClass().equals(TargetDataLine.class)) {
                        String name = info.getName();
                        LOGGER.debug("找到输入设备: {}", name);

                        boolean isStereoMix = name.toLowerCase().contains("stereo mix") ||
                                name.toLowerCase().contains("立体声混音");
                        devices.add(new AudioDevice(index++, name, info, isStereoMix));
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("跳过设备 {}: {}", info.getName(), e.getMessage());
            }
        }
        
        LOGGER.info("找到 {} 个音频输入设备", devices.size());
        devices.forEach(device -> 
            LOGGER.info("输入设备 {}: {}, 是否为立体声混音: {}", 
                       device.index(), device.name(), device.isStereoMix()));
        
        return devices;
    }
} 