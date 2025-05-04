package com.example.easypc.data.service;

import com.example.easypc.data.dto.SourceDto;
import com.example.easypc.data.repository.SourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SourceService {
    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    public List<SourceDto> getAllSources() {
        return sourceRepository.findAll().stream()
                .map(s -> new SourceDto(s.getId(), s.getSource(), s.getCategory()))
                .collect(Collectors.toList());
    }
}
