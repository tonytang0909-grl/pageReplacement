import java.util.HashMap;
import java.util.HashSet;

public class ClockMMU implements MMU {
    private int frames;
    private boolean debug;
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;
    private int clockHand;
    private int insertIndex;
    private HashMap<Integer, Integer> pageSet; // Store page and its use bit
    private HashSet<Integer> dirtyPages;
    private int[] pageTable;

    public ClockMMU(int frames) {
        this.frames = frames;
        this.pageSet = new HashMap<>();
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.debug = false;
        this.dirtyPages = new HashSet<>();
        this.clockHand = 0;
        this.insertIndex = 0;
        this.pageHits = 0;
        pageTable = new int[frames];
        for (int i = 0; i < frames; i++) {
            pageTable[i] = -1;
        }
    }

    public void setDebug() {
        this.debug = true;
    }

    public void resetDebug() {
        this.debug = false;
    }

    private void evictAndReplace(int page_number) {
        while (true) {
            int page = pageTable[clockHand];
            if (pageSet.get(page) == 0) {
                // Check if the page is dirty before evicting
                if (dirtyPages.contains(page)) {
                    diskWrites++;
                    debugPrint("Disk write " + page);
                    dirtyPages.remove(page);
                }
                // Evict the page
                pageSet.remove(page);
                pageTable[clockHand] = page_number;
                pageSet.put(page_number, 1);
                break;
            } else {
                pageSet.put(page, 0);
            }
            clockHand = (clockHand + 1) % frames; // Move clock hand in a circular manner
        }
    }

    public void readMemory(int page_number) {
        if (!pageSet.containsKey(page_number)) {
            debugPrint("Page fault " + page_number);
            pageFaults++;
            diskReads++;
            if (pageSet.size() == frames) {
                evictAndReplace(page_number);
            }
            pageTable[clockHand] = page_number;
            pageSet.put(page_number, 1);
            clockHand = (clockHand + 1) % frames;
        } else {
            pageHits++;
            pageSet.put(page_number, 1);
        }
        debugPrint("Reading " + page_number);

        //System.out.println(diskReads + " " + diskWrites + " " + pageFaults);
    }

    public void writeMemory(int page_number) {
        if (!pageSet.containsKey(page_number)) {
            debugPrint("Page fault " + page_number);
            pageFaults++;
            diskReads++;
            if (pageSet.size() == frames) {
                evictAndReplace(page_number);
            }
            pageTable[clockHand] = page_number;
            pageSet.put(page_number, 1);
            clockHand = (clockHand + 1) % frames;
        } else {
            pageHits++;
            pageSet.put(page_number, 1);
        }
        dirtyPages.add(page_number);
        debugPrint("Writing " + page_number);

        //System.out.println(diskReads + " " + diskWrites + " " + pageFaults);
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
