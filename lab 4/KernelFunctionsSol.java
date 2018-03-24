

public class KernelFunctions
{
	// Some variables and constants for COUNT algorithm
	private static final int UPDATECOUNT=3;    // for Additional reference bits algorithm
	private static boolean doingCount;         // Set to true when applying COUNT algorithm
        private static int countUpdate = 0;        // for implementing COUNT algorithm
	//******************************************************************
	//                 Methods for supporting page replacement
	//******************************************************************

	// the following method calls a page replacement method once
	// all allocated frames have been used up
	// DO NOT CHANGE this method
	public static void pageReplacement(int vpage, Process prc, Kernel krn)
	{
	   if(prc.pageTable[vpage].valid) return;   // no need to replace

	   if(!prc.areAllocatedFramesFull()) // room to get frames in allocated list
 	       addPageFrame(vpage, prc, krn);
	   else
	       pageReplAlgorithm(vpage, prc, krn);
	}

	// This method will all a page frame to the list of allocated 
	// frames for the process and load it with the virtual page
	// DO NOT CHANGE this method
	public static void addPageFrame(int vpage, Process prc, Kernel krn)
	{
	     int [] fl;  // new frame list
	     int freeFrame;  // a frame from the free list
	     int i;
	     // Get a free frame and update the allocated frame list
	     freeFrame = krn.getNextFreeFrame();  // gets next free frame
	     if(freeFrame == -1)  // list must be empty - print error message and return
	     {
		System.out.println("Could not get a free frame");
		return;
	     }
	     if(prc.allocatedFrames == null) // No frames allocated yet
	     {
		prc.allocatedFrames = new int[1];
		prc.allocatedFrames[0] = freeFrame;
	     }
	     else // some allocated but can get more
	     {
	        fl = new int[prc.allocatedFrames.length+1];
	        for(i=0 ; i<prc.allocatedFrames.length ; i++) fl[i] = prc.allocatedFrames[i];
	        fl[i] = freeFrame; // adds free frame to the free list
	        prc.allocatedFrames = fl; // keep new list
	     }
	     // update Page Table
	     prc.pageTable[vpage].frameNum = freeFrame;
	     prc.pageTable[vpage].valid = true;
	}

	// Calls to Replacement algorithm
	public static void pageReplAlgorithm(int vpage, Process prc, Kernel krn)
	{
	     doingCount = false;
	     switch(krn.pagingAlgorithm)
	     {
		case FIFO: pageReplAlgorithmFIFO(vpage, prc); break;
		case LRU: pageReplAlgorithmLRU(vpage, prc); break;
		case CLOCK: pageReplAlgorithmCLOCK(vpage, prc); break;
		case COUNT: pageReplAlgorithmCOUNT(vpage, prc); doingCount=true; break;
	     }
	}
	
	//--------------------------------------------------------------
	//  The following methods need modification to implement
	//  the three page replacement algorithms
	//  ------------------------------------------------------------
	// The following method is called each time an access to memory
	// is made (including after a page fault). It will allow you
	// to update the page table entries for supporting various
	// page replacement algorithms.
	public static void doneMemAccess(int vpage, Process prc, double clock)
	{
	    if(prc.pageTable[vpage].valid)
	    {
	        prc.pageTable[vpage].used = true;
		prc.pageTable[vpage].tmStamp = clock;
	    }
	    if(doingCount)  
	    {// only change count values by calling shiftCount when applying COUNT algorithm
	       countUpdate = (++countUpdate)%UPDATECOUNT;
	       if(countUpdate == 0) shiftCount(prc.pageTable);    // for additional reference bits algorithm
	    }
	}

	// FIFO page Replacement algorithm
	public static void pageReplAlgorithmFIFO(int vpage, Process prc)
	{
	   int vPageReplaced;  // Page to be replaced
	   int frame;	// frame to receive new page
	   // Find page to be replaced
	   frame = prc.allocatedFrames[prc.framePtr];   // get next available frame
	   vPageReplaced = findvPage(prc.pageTable,frame);     // find current page using it (i.e written to disk)
	   prc.pageTable[vPageReplaced].valid = false;  // Old page is replaced.
	   prc.pageTable[vpage].frameNum = frame;   // load page into the frame and update table
	   prc.pageTable[vpage].valid = true;             // make the page valid
	   prc.framePtr = (prc.framePtr+1) % prc.allocatedFrames.length;  // point to next frame in list
	}

	// CLOCK page Replacement algorithm
	public static void pageReplAlgorithmCLOCK(int vpage, Process prc)
	{
	   int vPageReplaced;  // Page to be replaced
	   int frame;	// frame to receive new page
	   // Find page to be replaced
	   boolean flag = true;
	   while(flag)
	   {
	      frame = prc.allocatedFrames[prc.framePtr];   // frame in buffer
	      vPageReplaced = findvPage(prc.pageTable,frame);
	      if(prc.pageTable[vPageReplaced].used)
	      {
		 prc.pageTable[vPageReplaced].used = false;
	      }
	      else  // found a replacement page
	      {
		 prc.pageTable[vPageReplaced].valid = false; // replacing the page
	         prc.pageTable[vpage].frameNum = frame;      // load page into the frame and update table
	         prc.pageTable[vpage].valid = true;          // make the page valid
		 flag = false;
	      }
	      prc.framePtr = (prc.framePtr+1) % prc.allocatedFrames.length;  // point to next frame in list
	   }
	}

	// LRU page Replacement algorithm
	public static void pageReplAlgorithmLRU(int vpage, Process prc)
	{
	   int ptr;
           int vPageReplaced;
	   
	   // Find first valid page in the page table
	   vPageReplaced = 0;
	   for(ptr = 0 ; ptr < prc.pageTable.length ; ptr++)
	   {
	       if(prc.pageTable[ptr].valid) 
	       {
		 vPageReplaced = ptr;
		 break;
	       }
	   }
	   // Check the rest of the page table
	   for( ; ptr<prc.pageTable.length ; ptr++)
	   {
	       if(prc.pageTable[ptr].valid)
	       {
	         if(prc.pageTable[ptr].tmStamp < prc.pageTable[vPageReplaced].tmStamp)
	             vPageReplaced = ptr; 
	       }
	   }
	   // Replace the page
	   prc.pageTable[vPageReplaced].valid = false; // replacing the page
	   prc.pageTable[vpage].frameNum = prc.pageTable[vPageReplaced].frameNum; // load page into the frame
	   prc.pageTable[vpage].valid = true;          // make the page valid
	}

	// finds the virtual page loaded in the specified frame fr
	public static int findvPage(PgTblEntry [] ptbl, int fr)
	{
	   int i;
	   for(i=0 ; i<ptbl.length ; i++)
	   {
	       if(ptbl[i].valid)
	       {
	          if(ptbl[i].frameNum == fr)
	          {
		      return(i);
	          }
	       }
	   }
	   System.out.println("Could not find frame number in Page Table "+fr);
	   return(-1);
	}

	// COUNT algorithm, based on additional reference bits algorithm (see section 9.4.5.1 in Silberschatz)
	// 1) periodically (every 3, i.e. UPDATECOUNT, memory accesses), used bit is shifted into "count"
	//    for each page in the page table (experimentation showed that increasing UPDATECOUNT to 4 
	//    decreases performance and decreasing UPDATECOUNT did not improve performance significantly).
	//    - used bit is then cleared
	//    - only 8 bits are used in count (using 16 bits only improved slighly performance while 
	//                                     using 4 bits decreased performance).
	// 2) when a page replacement is required (i.e. when a page fault occurs), the page with the
	//    smallest count value is selected. The smaller count value is interpreted as page being 
	//    used less recently.
	//    - Experimentation showed that using a FIFO pointer improved performance for selecting
	//      the first page with the smallest count when more than one page had the smallest count.
	// The method shiftCount is called every UPDATECOUNT memory accesses (see memAccessDone)
	// to accomplish the update of count according to item 1 above.
	// The method pageReplAlgorithmCOUNT will replace the page based on item 2 above.
	//
	public static void shiftCount(PgTblEntry [] pgTbl)
	{
           int ptr;
	   for(ptr=0 ; ptr < pgTbl.length ; ptr++)
	   {
	      if(pgTbl[ptr].valid)
	      {
		  pgTbl[ptr].count = pgTbl[ptr].count>>1;  // Shift right
		  if(pgTbl[ptr].used) pgTbl[ptr].count |= 0x80;  // set MSB to reference bit, i.e. used
		  //if(!pgTbl[ptr].used) pgTbl[ptr].count = pgTbl[ptr].count>>1;  // shift
		  pgTbl[ptr].used = false;   // clear the used bit
	      }
	   }
	}
	
	public static void pageReplAlgorithmCOUNT(int vpage, Process prc)
	{
	   int flsize = prc.allocatedFrames.length;  // frame list size 
	   int vPageReplaced;  // Page to be replaced
	   int vpageCurrent;  // a virtual page number
	   int frame;	// frame to receive new page
	   int i;

	   // Find page to be replaced
	   boolean flag = true;
	   vPageReplaced = findvPage(prc.pageTable,prc.allocatedFrames[prc.framePtr]);  // set to first page
	   i=(prc.framePtr+1)%flsize;
	   do
	   {
	      frame = prc.allocatedFrames[i];   // frame in buffer
	      vpageCurrent = findvPage(prc.pageTable,frame);
	      if(prc.pageTable[vpageCurrent].count < prc.pageTable[vPageReplaced].count)
		 vPageReplaced = vpageCurrent;
	      i = ++i%flsize;
	   }
	   while(i!=prc.framePtr);
	   // Now lets replace the page
	   prc.pageTable[vPageReplaced].valid = false; // replacing the page
	   prc.pageTable[vPageReplaced].count = 0;     // replacing the page
	   prc.pageTable[vpage].frameNum = prc.pageTable[vPageReplaced].frameNum; // load page into the frame
	   prc.pageTable[vpage].valid = true;          // make the page valid
	   flag = false;
	   prc.framePtr = (prc.framePtr+1) % prc.allocatedFrames.length;  // point to next frame in list
	}

	// *******************************************
	// The following method is provided for debugging purposes
	// Call it to display the various data structures defined
	// for the process so that you may examine the effect
	// of your page replacement algorithm on the state of the 
	// process.
        // Method for displaying the state of a process
	// *******************************************
	public static void logProcessState(Process prc)
	{
	   int i;

	   System.out.println("--------------Process "+prc.pid+"----------------");
	   System.out.println("Virtual pages: Total: "+prc.numPages+
			      " Code pages: "+prc.numCodePages+
			      " Data pages: "+prc.numDataPages+
			      " Stack pages: "+prc.numStackPages+
			      " Heap pages: "+prc.numHeapPages);
	   System.out.println("Simulation data: numAccesses left in cycle: "+prc.numMemAccess+
			      " Num to next change in working set: "+prc.numMA2ChangeWS);
           System.out.println("Working set is :");
           for(i=0 ; i<prc.workingSet.length; i++)
           {
              System.out.print(" "+prc.workingSet[i]);
           }
           System.out.println();
	   // page Table
	   System.out.println("Page Table");
	   if(prc.pageTable != null)
	   {
	      for(i=0 ; i<prc.pageTable.length ; i++)
	      {
		 if(prc.pageTable[i].valid) // its valid printout the data
		 {
	           System.out.println("   Page "+i+"(valid): "+
				      " Frame "+prc.pageTable[i].frameNum+
				      " Used "+prc.pageTable[i].used+
				      " count "+prc.pageTable[i].count+
				      " Time Stamp "+prc.pageTable[i].tmStamp);
		 }
		 else System.out.println("   Page "+i+" is invalid (i.e not loaded)");
	      }
	   }
	   // allocated frames
	   System.out.println("Allocated frames (max is "+prc.numAllocatedFrames+")"+
			      " (frame pointer is "+prc.framePtr+")");
	   if(prc.allocatedFrames != null)
	   {
	      for(i=0 ; i<prc.allocatedFrames.length ; i++)
 	           System.out.print(" "+prc.allocatedFrames[i]);
	   }
	   System.out.println();
           System.out.println("---------------------------------------------");
	}
}

// Page Table Entries
class PgTblEntry
{
	int frameNum;	// Frame number
	boolean valid;   // Valid Bit
	boolean used;    // Used Bit
        double tmStamp;  // Time Stamp
	long count;   // for counting
}

