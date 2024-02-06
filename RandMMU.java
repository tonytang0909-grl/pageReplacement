import java.util.HashSet;
import java.util.Random;

/**
* MMU using random selection replacement strategy
*/

public class RandMMU implements MMU {
    private int frames;
    private boolean debug;
    private Random random;
    private HashSet<Integer> pageSet; // To store currently loaded pages
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;
    private HashSet<Integer> dirtyPages;
    public RandMMU(int frames) {
        //todo
        this.frames = frames;
        this.random = new Random();
        this.pageSet = new HashSet<>();
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.pageHits = 0;
        this.debug = false;
        this.dirtyPages = new HashSet<>();
    }
    
    public void setDebug() {
        //todo
        this.debug = true;
    }
    
    public void resetDebug() {
        //todo
        this.debug = false;
    }
    
    public void readMemory(int page_number) {
        //todo
        if (!pageSet.contains(page_number)) {
            debugPrint("Page fault at page " + page_number);
            pageFaults++;
            if (pageSet.size() == frames) {
                // Randomly select a page for eviction
                int evictedPage = (int) pageSet.toArray()[random.nextInt(pageSet.size())];
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("Disk write " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
                pageSet.remove(evictedPage);
            }
            pageSet.add(page_number);
            diskReads++;
        } else {
            pageHits++;
            debugPrint("Page " + page_number + " found in memory (read).");
        }
    }
    
    public void writeMemory(int page_number) {
        //todo
        // This method can be similar to readMemory, but you also account for a disk write when replacing a "dirty" page.
        // For simplicity, let's assume every written page is dirty and leads to a disk write when evicted.
        dirtyPages.add(page_number);
        if (!pageSet.contains(page_number)) {
            pageFaults++;
            debugPrint("Page fault" + page_number);
            if (pageSet.size() == frames) {
                int evictedPage = (int) pageSet.toArray()[random.nextInt(pageSet.size())];
                if (dirtyPages.contains(evictedPage)) {
                    diskWrites++;
                    debugPrint("Disk write " + evictedPage);
                    dirtyPages.remove(evictedPage);
                }
                pageSet.remove(evictedPage);
                diskWrites++;  // Assuming evicted page is dirty after a write
            }
            pageSet.add(page_number);
            diskReads++;
        } else {
            pageHits++;
        }

    }
    
    public int getTotalDiskReads() {
        //todo
        return diskReads;
    }
    
    public int getTotalDiskWrites() {
        //todo
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