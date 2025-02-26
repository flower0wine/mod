package com.example.fabricmod.audio;

import javax.sound.sampled.Mixer;

public record AudioDevice(int index, String name, Mixer.Info info, boolean isStereoMix) {
} 