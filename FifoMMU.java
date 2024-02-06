import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class FifoMMU implements MMU{
    private int frames;
    private boolean debug;
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;
    private Queue<Integer> pageQueue;
    private HashSet<Integer> memorySet;
    private HashSet<Integer> dirtyPages;

    public FifoMMU(int frames) {
        this.frames = frames;
        this.pageQueue = new LinkedList<>();
        this.memorySet = new HashSet<>();
        this.dirtyPages = new HashSet<>();
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.pageHits = 0;
        this.debug = false;
    }

    @Override
    public void readMemory(int page_number) {
        if (!memorySet.contains(page_number)) {
            debugPrint("Page fault        " + page_number);
            pageFaults++;
            diskReads++;
            if (memorySet.size() == frames) {
                int evictedPage = pageQueue.remove();
                memorySet.remove(evictedPage);
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("disk write          " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
            }
            pageQueue.add(page_number);
            memorySet.add(page_number);
        } else {
            pageHits++;
        }
        debugPrint("reading           " + page_number);
    }



    @Override
    public void writeMemory(int page_number) {
        if (!memorySet.contains(page_number)) {
            debugPrint("Page fault        " + page_number);
            pageFaults++;
            diskReads++;
            if (memorySet.size() == frames) {
                int evictedPage = pageQueue.remove();
                memorySet.remove(evictedPage);
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("disk write          " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
            }
            pageQueue.add(page_number);
            memorySet.add(page_number);
        } else {
            pageHits++;
        }
        debugPrint("Writing           " + page_number);
        dirtyPages.add(page_number);
    }

    @Override
    public void setDebug() {
        this.debug = true;
    }

    @Override
    public void resetDebug() {
        this.debug = false;
    }

    @Override
    public int getTotalDiskReads() {
        return diskReads;
    }

    @Override
    public int getTotalDiskWrites() {
        return diskWrites;
    }

    @Override
    public int getTotalPageFaults() {
        return pageFaults;
    }

    @Override
    public int getPageHits() {
        return pageHits;
    }

    private void debugPrint(String s) {
        if (debug) {
            System.out.println(s);
        }
    }
}
