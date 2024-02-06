import java.util.HashMap;
import java.util.LinkedHashSet;

public class ARCMMU implements MMU {
    private int frames;
    private boolean debug;
    private int diskReads;
    private int diskWrites;
    private int pageFaults;
    private int pageHits;

    private LinkedHashSet<Integer> T1;
    private LinkedHashSet<Integer> T2;
    private int p;  // Target size for T1

    private HashMap<Integer, Boolean> dirtyPages;

    public ARCMMU(int frames) {
        this.frames = frames;
        this.debug = false;
        this.diskReads = 0;
        this.diskWrites = 0;
        this.pageFaults = 0;
        this.pageHits = 0;
        this.T1 = new LinkedHashSet<>();
        this.T2 = new LinkedHashSet<>();
        this.p = 0;
        this.dirtyPages = new HashMap<>();
    }

    private void replace(int page) {
        if (!T1.isEmpty() && (T1.size() > p || (!T2.contains(page) && T1.size() == p))) {
            int last = T1.iterator().next();
            T1.remove(last);
            if (dirtyPages.containsKey(last)) {
                diskWrites++;
                dirtyPages.remove(last);
            }
        } else {
            int last = T2.iterator().next();
            T2.remove(last);
            if (dirtyPages.containsKey(last)) {
                diskWrites++;
                dirtyPages.remove(last);
            }
        }
    }

    public void readMemory(int page) {
        if (!T1.contains(page) && !T2.contains(page)) {
            pageFaults++;
            diskReads++;
            if (T1.size() + T2.size() == frames) {
                replace(page);
            }
            T1.add(page);
        } else if (T1.contains(page)) {
            pageHits++;
            T1.remove(page);
            T2.add(page);
        } else if (T2.contains(page)) {
            pageHits++;
            // Already in T2, just update it
        }
        if (T1.contains(page) && T2.contains(page)) {
            if (T1.size() / (double) frames > p / (double) frames) {
                p++;
            } else {
                p--;
            }
        }
        debugPrint("Reading " + page);
    }

    public void writeMemory(int page) {
        readMemory(page);  // Similar logic, but marking page dirty
        dirtyPages.put(page, true);
        debugPrint("Writing " + page);
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
