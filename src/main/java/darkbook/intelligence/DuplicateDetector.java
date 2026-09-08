package darkbook.intelligence;

import java.util.HashSet;
import java.util.Set;

public class DuplicateDetector {

    private final Set<String> visitedVideos = new HashSet<>();

    public boolean alreadyVisited(String url){

        if(visitedVideos.contains(url))
            return true;

        visitedVideos.add(url);

        return false;

    }

    public int getVisitedCount(){
        return visitedVideos.size();
    }

}