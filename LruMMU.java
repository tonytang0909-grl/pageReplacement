import java.util.HashSet;
import java.util.LinkedList;

public class LruMMU implements MMU {
    private int frames;
    private boolean debug;
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;
    private LinkedList<Integer> pageQueue;
    private HashSet<Integer> memorySet;
    //private HashSet<Integer> diskSet;
    private HashSet<Integer> dirtyPages;  // This will store pages in memory that are written to.



    public LruMMU(int frames) {
        this.frames = frames;
        this.pageQueue = new LinkedList<>();
        this.memorySet = new HashSet<>();
        //this.diskSet = new HashSet<>();
        this.dirtyPages = new HashSet<>();
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.debug = false;
        this.pageHits = 0;
    }

    public void setDebug() {
        this.debug = true;
    }

    public void resetDebug() {
        this.debug = false;
    }

    public void readMemory(int page_number) {
        if (!memorySet.contains(page_number)) {
            debugPrint("Page fault        " + page_number);
            pageFaults++;
            diskReads++;
            if (memorySet.size() == frames) {
                int evictedPage = pageQueue.removeFirst();
                memorySet.remove(evictedPage);
                //diskSet.add(evictedPage);
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("disk write          " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
            }
            pageQueue.addLast(page_number);
            memorySet.add(page_number);
        } else {
            pageHits++;
            pageQueue.remove(Integer.valueOf(page_number));
            pageQueue.addLast(page_number);
        }
        debugPrint("reading           " + page_number);
    }

    public void writeMemory(int page_number) {
        if (!memorySet.contains(page_number)) {
            debugPrint("Page fault        " + page_number);
            debugPrint("Writing           " + page_number);
            pageFaults++;
            //dirtyPages.add(page_number);  // Mark the page as dirty.
            diskReads++;
            dirtyPages.add(page_number);
            if (memorySet.size() == frames) {
                int evictedPage = pageQueue.removeFirst();
                memorySet.remove(evictedPage);
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("disk write          " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
            }
            memorySet.add(page_number);
            pageQueue.addLast(page_number);
        } else {
            pageHits++;
            pageQueue.remove(Integer.valueOf(page_number));
            dirtyPages.add(page_number);  // Mark the page as dirty.
            pageQueue.addLast(page_number);
            debugPrint("writting          " + page_number);
        }
    }

    public int getTotalDiskReads() {
        return diskReads;
    }

    public int getTotalDiskWrites() {
        return this.diskWrites;
    }

    public int getTotalPageFaults() {
        return pageFaults;
    }
    public int getPageHits() {
        return this.pageHits;
    }

    protected void debugPrint(String message) {
        if (debug) {
            System.out.println(message);
        }
    }
}
