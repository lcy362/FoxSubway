package com.solo.subway.router;

import com.solo.subway.data.PathInfo;
import com.solo.subway.data.Station;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

@Service
@Slf4j
@ConditionalOnProperty(name = "routeType", havingValue = "distance")
public class DijkstraDistanceRouter extends AbstractRouter{

    @Override
    protected void handleAllPath(Map<String, PathInfo> knownPath, Map<String, PathInfo> waitingPath, String originId, Map<String, Station> stations) {
        PathInfo now = knownPath.get(originId);
        while (now != null) {
            //从起点开始处理
            Station currentStation = stations.get(now.getStationId());

            final PathInfo nowPath = now;
            currentStation.getNextStations().stream()
                    .filter(nextId -> !knownPath.containsKey(nextId))
                    .map(waitingPath::get)
                    .filter(nextPath -> nextPath.getDistance() > nowPath.getDistance() + currentStation.getNextStationDistance().get(nextPath.getStationId()))
                    .forEach(nextPath -> {
                        nextPath.setDistance(nowPath.getDistance() + currentStation.getNextStationDistance().get(nextPath.getStationId()));
                        log.debug("set " + stations.get(nextPath.getStationId()).getName() + " distance to " + nextPath.getDistance());
                        nextPath.setDetail(new ArrayList<>(nowPath.getDetail()));
                        nextPath.addNodeToPath(nextPath.getStationId());
                    });

            //选出离当前站点距离最小的站，作为下一个要处理的站点
            now = waitingPath.values().stream()
                    .min(Comparator.comparingDouble(PathInfo::getDistance))
                    .orElse(null);

            if (now != null) {
                waitingPath.remove(now.getStationId());
                knownPath.put(now.getStationId(), now);
                log.debug(stations.get(now.getStationId()).getName() + " distance is " + now.getLength());
            }

        }
    }
}
