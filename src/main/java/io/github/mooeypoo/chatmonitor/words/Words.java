package io.github.mooeypoo.chatmonitor.words;

import java.util.Map;

import com.google.common.collect.SetMultimap;

public record Words(
        Map<String, String> wordmap,
        SetMultimap<String, String> mapWordsInCommands
) {
}
