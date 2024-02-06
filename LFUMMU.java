import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class LFUMMU implements MMU {
    private int frames;
    private boolean debug;
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;

    private HashMap<Integer, Integer> frequencyMap;  // To store frequency of pages.
    private PriorityQueue<Integer> leastFrequentlyUsedQueue;
    private HashSet<Integer> inMemoryPages;
    private HashSet<Integer> dirtyPages;

    public LFUMMU(int frames) {
        this.frames = frames;
        this.debug = false;
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.pageHits = 0;
        this.frequencyMap = new HashMap<>();
        this.inMemoryPages = new HashSet<>();
        this.dirtyPages = new HashSet<>();
        this.leastFrequentlyUsedQueue = new PriorityQueue<>(
                (a, b) -> frequencyMap.get(a) - frequencyMap.get(b)
        );
    }

    private void evictAndReplace(int pageNumber) {
        int evictPage = leastFrequentlyUsedQueue.poll();
        inMemoryPages.remove(evictPage);
        frequencyMap.remove(evictPage);
        if (dirtyPages.contains(evictPage)) {
            diskWrites++;
            dirtyPages.remove(evictPage);
        }
        inMemoryPages.add(pageNumber);
        frequencyMap.put(pageNumber, 1);
        leastFrequentlyUsedQueue.add(pageNumber);
    }

    public void readMemory(int pageNumber) {
        if (!inMemoryPages.contains(pageNumber)) {
            pageFaults++;
            diskReads++;
            if (inMemoryPages.size() == frames) {
                evictAndReplace(pageNumber);
            } else {
                inMemoryPages.add(pageNumber);
                frequencyMap.put(pageNumber, 1);
                leastFrequentlyUsedQueue.add(pageNumber);
            }
        } else {
            pageHits++;
            frequencyMap.put(pageNumber, frequencyMap.get(pageNumber) + 1);
            // We might need to update the priority queue since frequency has changed
            leastFrequentlyUsedQueue.remove(pageNumber);
            leastFrequentlyUsedQueue.add(pageNumber);
        }
        debugPrint("Reading " + pageNumber);
    }

    public void writeMemory(int pageNumber) {
        if (!inMemoryPages.contains(pageNumber)) {
            pageFaults++;
            diskReads++;
            if (inMemoryPages.size() == frames) {
                evictAndReplace(pageNumber);
            } else {
                inMemoryPages.add(pageNumber);
                frequencyMap.put(pageNumber, 1);
                leastFrequentlyUsedQueue.add(pageNumber);
            }
        } else {
            pageHits++;
            frequencyMap.put(pageNumber, frequencyMap.get(pageNumber) + 1);
            // We might need to update the priority queue since frequency has changed
            leastFrequentlyUsedQueue.remove(pageNumber);
            leastFrequentlyUsedQueue.add(pageNumber);
        }
        dirtyPages.add(pageNumber);
        debugPrint("Writing " + pageNumber);
    }

    @Override
    public void setDebug() {
        this.debug = true;
    }

    @Override
    public void resetDebug() {
        this.debug = false;
    }

    public int getTotalDiskReads() {
        return diskReads;
    }

    public int getTotalDiskWrites() {
        return diskWrites;
    }

    public int getTotalPageFaults() {
        return pageFaults;
    }

    @Override
    public int getPageHits() {
        return pageHits;
    }

    protected void debugPrint(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
