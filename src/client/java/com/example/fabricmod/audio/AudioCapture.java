package com.example.fabricmod.audio;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jtransforms.fft.FloatFFT_1D;
import java.util.Arrays;

public class AudioCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioCapture.class);
    private TargetDataLine line;
    private volatile boolean running = false;
    private static final int FFT_SIZE = 2048;  // 与Web Audio API一致
    private static final int BANDS = 1024;     // frequencyBinCount = fftSize/2
    private final float[] fftBuffer = new float[FFT_SIZE];
    private final float[] spectrum = new float[BANDS];
    private final FloatFFT_1D fft = new FloatFFT_1D(FFT_SIZE);
    private static final float SCALE = 1.0f / 32768.0f;
    private final byte[] buffer = new byte[FFT_SIZE * 2];
    private static final float NOISE_THRESHOLD = 0.2f;  // 噪音阈值
    private static final int SILENCE_THRESHOLD = 10;     // 判定为静音的采样数阈值

    private static final float VOLUME_SCALE = 1.0f / 32768.0f;  // 16位音频的缩放因子
    private static final float DB_MIN = -60.0f;  // 最小分贝值
    private static final float DB_MAX = 0.0f;    // 最大分贝值

    public void tick() {
        if (!running || line == null) return;

        try {
            // 读取新数据
            int count = line.read(buffer, 0, buffer.length);
            if (count <= 0) {
                Arrays.fill(spectrum, 0.0f);
                return;
            }

            // 检查是否为噪音
            int silenceCount = 0;
            float maxSample = 0.0f;

            // 将字节数据转换为浮点数并检查振幅
            for (int i = 0; i < FFT_SIZE && i * 2 < count; i++) {
                short sample = (short) ((buffer[i * 2 + 1] << 8) | (buffer[i * 2] & 0xFF));
                float normalizedSample = Math.abs(sample * SCALE);
                maxSample = Math.max(maxSample, normalizedSample);
                
                if (normalizedSample < NOISE_THRESHOLD) {
                    silenceCount++;
                }
                
                fftBuffer[i] = sample * SCALE;
            }

            // 如果大部分样本都小于噪音阈值，认为是静音
            if (silenceCount > FFT_SIZE - SILENCE_THRESHOLD || maxSample < NOISE_THRESHOLD) {
                Arrays.fill(spectrum, 0.0f);
                return;
            }

            // 执行FFT
            fft.realForward(fftBuffer);
            processSpectrum(fftBuffer);
            
        } catch (Exception e) {
            LOGGER.error("处理音频数据时出错", e);
            stop();
        }
    }

    private void processSpectrum(float[] fftData) {
        synchronized (spectrum) {
            Arrays.fill(spectrum, 0.0f);
            
            // 直接使用FFT结果，不需要复杂的频段划分
            for (int i = 0; i < BANDS; i++) {
                if (2*i + 1 < fftData.length) {
                    float re = fftData[2*i];
                    float im = fftData[2*i + 1];
                    float magnitude = (float) Math.sqrt(re * re + im * im);
                    
                    magnitude = (magnitude + 60) / 40;  // 假设最小值为-60dB
                    
                    spectrum[i] = magnitude;
                }
            }
        }
    }

    private float calculateVolume(byte[] audioData) {
        // 计算音频数据的RMS（均方根）值
        long sum = 0;
        for (int i = 0; i < audioData.length; i += 2) {
            short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
            sum += sample * sample;
        }
        float rms = (float) Math.sqrt((double) sum / ((double) audioData.length / 2));

        // 设置基准噪音值
        final float NOISE_FLOOR = 260.0f;
        
        // 如果RMS小于等于基准噪音值，直接返回0
        if (rms <= NOISE_FLOOR) {
            LOGGER.debug("音量: RMS={}, 低于噪音基准值", rms);
            return 0.0f;
        }
        
        // 将RMS值相对于噪音基准值进行归一化
        float normalizedRms = (rms - NOISE_FLOOR) / (32768.0f - NOISE_FLOOR);
        
        // 转换为分贝值
        float db = 20 * (float) Math.log10(normalizedRms);
        
        // 将分贝值归一化到0-1范围
        float normalizedDb = (Math.max(DB_MIN, Math.min(DB_MAX, db)) - DB_MIN) / (DB_MAX - DB_MIN);
        
        LOGGER.debug("音量: RMS={}, dB={}, normalized={}", rms, db, normalizedDb);
        return normalizedDb;
    }

    public float getCurrentVolume() {
        if (!running || line == null) return 0.0f;
        
        int bytesRead = line.read(buffer, 0, buffer.length);
        if (bytesRead > 0) {
            return calculateVolume(Arrays.copyOf(buffer, bytesRead));
        }
        return 0.0f;
    }

    public void start(Mixer.Info mixerInfo) {
        stop();
        
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (mixerInfo == null) {
                throw new IllegalStateException("未指定音频设备");
            }

            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (!mixer.isLineSupported(info)) {
                throw new IllegalStateException("音频设备不支持指定的格式");
            }

            line = (TargetDataLine) mixer.getLine(info);
            line.open(format, buffer.length);
            line.start();

            running = true;
            LOGGER.info("成功启动音频捕获");
            
        } catch (LineUnavailableException e) {
            LOGGER.error("启动音频捕获失败", e);
            throw new RuntimeException("无法启动音频捕获", e);
        }
    }

    public void stop() {
        running = false;
        if (line != null) {
            try {
                line.stop();
                line.flush();
                line.close();
            } catch (Exception e) {
                LOGGER.error("关闭音频设备时出错", e);
            } finally {
                line = null;
            }
        }
        // 清空频谱数据
        Arrays.fill(spectrum, 0.0f);
    }

    public float[] getSpectrum() {
        synchronized (spectrum) {
            return spectrum.clone();
        }
    }

    public static List<AudioDevice> getAvailableInputs() {
        List<AudioDevice> devices = new ArrayList<>();
        int index = 0;
        
        try {
            // 获取所有混音器信息
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                try {
                    Mixer mixer = AudioSystem.getMixer(info);
                    Line.Info[] lineInfos = mixer.getTargetLineInfo();
                    
                    // 检查是否支持我们需要的音频格式
                    if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
                        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
                        
                        if (mixer.isLineSupported(lineInfo)) {
                            String name = info.getName();
                            String description = info.getDescription();
                            
                            // 标记立体声混音设备
                            boolean isStereoMix = name.toLowerCase().contains("stereo mix") || 
                                                name.toLowerCase().contains("立体声混音") ||
                                                name.toLowerCase().contains("what u hear") ||
                                                name.toLowerCase().contains("什么听到什么录") ||
                                                description.toLowerCase().contains("立体声混音");
                            
                            String deviceType = isStereoMix ? "系统音频" : "麦克风";
                            String displayName = String.format("%s (%s)", name, deviceType);
                            
                            devices.add(new AudioDevice(index++, displayName, info, isStereoMix));
                            LOGGER.info("找到音频设备: " + displayName);
                        }
                    }
                } catch (Exception e) {
                    // 跳过无法访问的设备
                    LOGGER.debug("跳过设备 " + info.getName() + ": " + e.getMessage());
                }
            }
            
            if (devices.isEmpty()) {
                LOGGER.warn("未找到任何可用的音频输入设备！");
            }
        } catch (Exception e) {
            LOGGER.error("获取音频设备列表时出错", e);
        }
        
        return devices;
    }
}