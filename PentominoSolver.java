import java.util.*;
public class PentominoSolver{
	/*
		The following array describes all 63 possible arragements
		(so including reflections and rotations) of pentomino shapes.
		The array was first created by David J. Eck(http://math.hws.edu/eck/) and checked by us. */  
	public static ArrayList<Integer[]> pieces = new ArrayList<Integer[]>();
	private static int[] values={3,4,5};
	private static int[] used={0,0,0};
	private static int[] available={100,100,100};
	private static int value=0;
	private static int maxValue=0;
	private static int coveredSpace=0;
	private static double GRIDSIZE=0.5;
	public final static double[][] BOXES={{1, 1,1,2},{1, 1,2,1},{1, 2,1,1},
                                        {2, 1,1.5,2},{2, 1.5,2,1},{2, 2,1,1.5},{2, 1,2,1.5},{2, 2,1.5,1},{2, 1.5,1,2},
                                        {3, 1.5,1.5,1.5}};
    public static int height = 8;
	public static int length = 33;
	public static int width= 5;
	public static char[] letters={'t','l','p'}; // lists the pentominos - needed to convert the user input (in form of a letter) to valid identifier of a pentomino shape
	public static int solutionCount=0;
	public static void main(String[] args){
		int[][] givenpieces= new int[0][0]; //initializes the pieces we actually gonna use for the process.
		for (double[] box:BOXES){
            standardize(box); //adapts the box size input(in m) to the Gridsize(0.5m)...
            Integer[] coordinates = convertToShape(box[1], box[2], box[3],(int) box[0]);
            System.out.println(Arrays.toString(coordinates));
            pieces.add(coordinates);
        }

		//if((height*length)%5!=0 || (height*length)>60){System.out.println("There is no solution for a cargospace with the given dimensions.");return;} // checks whatever the cargospace dimensions are valid

		int[][][] cargoSpace=createCargoSpace(height, length, width); // makes a new cargospace with the given dimensions
		int[][][] optimal=cargoSpace;
		long start = System.currentTimeMillis(); // starts measuring the time to see how long the program needs to determine
		solve(0,0,0,cargoSpace,pieces,length,height,width,optimal);
		long time = System.currentTimeMillis() - start; // measures the time the programm took to run
		System.out.println("It took the program "+(double)time/1000+" seconds to figure this out");
	}
	
	/*create a new cargospace*/
	public static int[][][] createCargoSpace(int height, int length,int width){
		int[][][] cargoSpace = new int[height][length][width];
		return cargoSpace;
	}
	
	/*trys to put the pentominoes of the given orientation on the cargospace - returns true if this is possible and false if not*/
	public static boolean canPlace(int p, int row, int col,int depth, int[][][] cargospace, ArrayList<Integer[]> pieces,int length,int height, int width) {
        if (cargospace[row][col][depth] != 0) //supposed empty square of cargospace, (0,0) of piece is filled
			return false;
        for (int i = 1; i+2 < pieces.get(p).length; i += 3) {
			 // i is the row, i+1 is the column
            if (row+pieces.get(p)[i] < 0 || //if it is above the cargospace
            	row+pieces.get(p)[i] >= length || //if it is below the cargospace
            	col+pieces.get(p)[i+1] < 0 || //if it is to the left of the cargospace
            	col+pieces.get(p)[i+1] >= height ||
            	depth+pieces.get(p)[i+2] < 0||
            	depth+pieces.get(p)[i+2] >= width) //if it is to the right of the cargospace
				{return false;}

            else if (cargospace[row+pieces.get(p)[i]][col+pieces.get(p)[i+1]][depth+pieces.get(p)[i+2]] != 0)  // checks if one of the squares needed is already occupied
				{return false;}
        }
        value+=values[pieces.get(p)[0]-1];
        used[pieces.get(p)[0]-1]++;
        //System.out.println(used[pieces.get(p)[0]-1]);
        cargospace[row][col][depth] = pieces.get(p)[0];
        for (int i = 1; i+2 < pieces.get(p).length; i += 3){
			cargospace[row+pieces.get(p)[i]][col+pieces.get(p)[i+1]][depth+pieces.get(p)[i+2]] = pieces.get(p)[0]; // puts the pentomino on the cargospace by putting the identifier in the corresponding block.
			coveredSpace++;
		}
        return true;
    }
	
	/* Remove a pentomino from the cargospace by setting its coordinates to zero*/
	public static void backOne(int p, int row, int col,int depth, int[][][] cargospace, ArrayList<Integer[]> pieces) {
		used[pieces.get(p)[0]-1]--;
        cargospace[row][col][depth] = 0;
        for (int i = 1; i+2 < pieces.get(p).length; i += 3){
			cargospace[row+pieces.get(p)[i]][col+pieces.get(p)[i+1]][depth+pieces.get(p)[i+2]] = 0;
			coveredSpace--;
		}
    }

	/* the actuall problem solving part*/																	//x-Axis   //y-Axis	 //z-Axis
	public static void solve(int row, int col,int depth, int[][][] cargospace, ArrayList<Integer[]> pieces, int length, int height,int width,int[][][] optimal) {
        if(used[0]==available[0]&&used[1]==available[1]&&used[2]==available[2]){
        	System.out.println("All pieces used up.");
        	return;
        }
        for (int p=0; p<pieces.size(); p++){
        	if(used[pieces.get(p)[0]-1]==available[pieces.get(p)[0]-1])
        		continue; //continue in for-loop, try different piece
		    if (!canPlace(p,row,col,depth,cargospace,pieces,height,length,width)){
		    	//System.out.println("Filled!");
		    	if(value>maxValue){
		    		optimal=cargospace;
		    		maxValue=value;
		    		System.out.println(maxValue);
		    	}
		    	if(isCompletlyFilled(cargospace))
		    		System.out.println("Cargospace is completely filled!");
		    	value=0;
		        continue; //continue in for-loop, try different piece
		    }

			else {
				int followingRow = row;  // find next empty space, going left-to-right then top-to-bottom
				int followingCol = col;
				int followingDepth = depth;
				//System.out.println(col+","+row+","+depth);
				while (cargospace[followingRow][followingCol][followingDepth] != 0) { // find the next empty square on the cargospace
					followingCol++;
					if (followingCol >= length){
						followingRow++;
						followingCol = 0;}
						if (followingRow >= height){
							followingDepth++;
							followingRow = 0;}
							if (followingDepth >= width)
								System.out.println("This shouldn't happen. Out of cargospace.");
				}
				solve(followingRow,followingCol,followingDepth,cargospace,pieces,length,height,width,optimal);  // try to complete the solution
			}
			backOne(p,row,col,depth,cargospace,pieces);  // go back again and try it with a diffrent piece
        } 
    }

    public static boolean isCompletlyFilled(int[][][] cargospace){
    	if(coveredSpace==height*length*width)
    		return true;
		return false;
	}

	public static Integer[] convertToShape(double i,double j,double k,int id){
        Integer[] shape=new Integer[(int)(i*j*k*3+1)];
        shape[0]=id;
        int co=0;
        for(int a=0;a<k;a++)
            for(int b=0;b<j;b++)
                for(int c=0;c<i;c++){
                    shape[co*3+1]=c;
                    shape[co*3+2]=b;
                    shape[co*3+3]=a;
                    co++;
                }
        return shape;
    }

    public static void standardize(double[] list){
        for(int i=1;i<list.length;i++)
            list[i]/=GRIDSIZE;
    }

}