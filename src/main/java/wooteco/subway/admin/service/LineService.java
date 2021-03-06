package wooteco.subway.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.LineStation;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineDetailResponse;
import wooteco.subway.admin.dto.LineRequest;
import wooteco.subway.admin.dto.LineStationCreateRequest;
import wooteco.subway.admin.dto.WholeSubwayResponse;
import wooteco.subway.admin.exception.LineNotFoundException;
import wooteco.subway.admin.repository.LineRepository;
import wooteco.subway.admin.repository.StationRepository;

@Service
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public Line save(Line line) {
        return lineRepository.save(line);
    }

    public List<Line> showLines() {
        return lineRepository.findAll();
    }

    public void updateLine(Long id, LineRequest request) {
        Line persistLine = lineRepository.findById(id).orElseThrow(LineNotFoundException::new);
        persistLine.update(request.toLine());
        lineRepository.save(persistLine);
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long id, LineStationCreateRequest request) {
        Line line = lineRepository.findById(id).orElseThrow(LineNotFoundException::new);
        LineStation lineStation = new LineStation(request.getPreStationId(), request.getStationId(),
                request.getDistance(), request.getDuration());
        line.addLineStation(lineStation);

        lineRepository.save(line);
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = lineRepository.findById(lineId).orElseThrow(LineNotFoundException::new);
        line.removeLineStationById(stationId);
        lineRepository.save(line);
    }

    public LineDetailResponse findLineWithStationsById(Long id) {
        Line line = lineRepository.findById(id).orElseThrow(LineNotFoundException::new);
        List<Station> stations = stationRepository.findAllById(line.getLineStationsId());
        return LineDetailResponse.of(line, stations);
    }

    public WholeSubwayResponse wholeLines() {
        List<Line> lines = lineRepository.findAll();

        List<LineDetailResponse> lineDetailResponses = lines.stream()
                .map(this::findLineDetails)
                .collect(Collectors.toList());

        return new WholeSubwayResponse(lineDetailResponses);
    }

    private LineDetailResponse findLineDetails(Line line) {
        return LineDetailResponse.of(line, stationRepository.findAllById(line.getLineStationsId()));
    }
}
