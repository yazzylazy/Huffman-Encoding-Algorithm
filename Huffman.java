
import java.io.*;
import java.util.ArrayList;

import net.datastructures.*;

/**
 * Class Huffman that provides huffman compression encoding and decoding of files
 * @author Yasin Elmi 2022
 *
 */

public class Huffman {

	/**
	 * 
	 * Inner class Huffman Node to Store a node of Huffman Tree
	 *
	 */
	private class HuffmanTreeNode { 
	    private int character;      // character being represented by this node (applicable to leaves)
	    private int count;          // frequency for the subtree rooted at node
	    private HuffmanTreeNode left;  // left/0  subtree (NULL if empty)
	    private HuffmanTreeNode right; // right/1 subtree subtree (NULL if empty)
	    public HuffmanTreeNode(int c, int ct, HuffmanTreeNode leftNode, HuffmanTreeNode rightNode) {
	    	character = c;
	    	count = ct;
	    	left = leftNode;
	    	right = rightNode;
	    }
	    public int getChar() { return character;}
	    public Integer getCount() { return count; }
	    public HuffmanTreeNode getLeft() { return left;}
	    public HuffmanTreeNode getRight() { return right;}
		public boolean isLeaf() { return left==null ; } // since huffman tree is full; if leaf=null so must be right
	}
	
	/**
	 * 
	 * Auxiliary class to write bits to an OutputStream
	 * Since files output one byte at a time, a buffer is used to group each output of 8-bits
	 * Method close should be invoked to flush half filed buckets by padding extra 0's
	 */
	private class OutBitStream {
		OutputStream out;
		int buffer;
		int buffCount;
		public OutBitStream(OutputStream output) { // associates this to an OutputStream
			out = output;
			buffer=0;
			buffCount=0;
		}
		public void writeBit(int i) throws IOException { // write one bit to Output Stream (using byte buffer)
		    buffer=buffer<<1;
		    buffer=buffer+i;
		    buffCount++;
		    if (buffCount==8) { 
		    	out.write(buffer); 
		    	//System.out.println("buffer="+buffer);
		    	buffCount=0;
		    	buffer=0;
		    }
		}
		
		public void close() throws IOException { // close output file, flushing half filled byte
			if (buffCount>0) { //flush the remaining bits by padding 0's
				buffer=buffer<<(8-buffCount);
				out.write(buffer);
			}
			out.close();
		}
		
 	}
	
	/**
	 * 
	 * Auxiliary class to read bits from a file
	 * Since we must read one byte at a time, a buffer is used to group each input of 8-bits
	 * 
	 */
	private class InBitStream {
		InputStream in;
		int buffer;    // stores a byte read from input stream
		int buffCount; // number of bits already read from buffer
		public InBitStream(InputStream input) { // associates this to an input stream
			in = input;
			buffer=0; 
			buffCount=8;
		}
		public int readBit() throws IOException { // read one bit to Output Stream (using byte buffer)
			if (buffCount==8) { // current buffer has already been read must bring next byte
				buffCount=0;
				buffer=in.read(); // read next byte
				if (buffer==-1) return -1; // indicates stream ended
			}
			int aux=128>>buffCount; // shifts 1000000 buffcount times so aux has a 1 is in position of bit to read
			//System.out.println("aux="+aux+"buffer="+buffer);
			buffCount++;
			if ((aux&buffer)>0) return 1; // this checks whether bit buffcount of buffer is 1
			else return 0;
			
		}

	}
	
	/**
	 * Builds a frequency table indicating the frequency of each character/byte in the input stream
	 * @param input is a file where to get the frequency of each character/byte
	 * @return freqTable a frequency table must be an ArrayList<Integer? such that freqTable.get(i) = number of times character i appears in file 
	 *                   and such that freqTable.get(256) = 1 (adding special character representing"end-of-file")
	 * @throws IOException indicating errors reading input stream
	 */
	
	private ArrayList<Integer> buildFrequencyTable(InputStream input) throws IOException{
		ArrayList<Integer> freqTable= new ArrayList<Integer>(257); // declare frequency table
		for (int i=0; i<257;i++) freqTable.add(i,0); // initialize frequency values with 0

		
		freqTable.set(256,1);  // adds frequency of last input

		int i = input.read();  // read the byte
		while(i!=-1){
			
			freqTable.set(i, freqTable.get(i)+1); //increases the frequency of selected character 
			i = input.read();   // read the next byte
			
		}
		
		return freqTable; // return computer frequency table
	}

	/**
	 * Create Huffman tree using the given frequency table; the method requires a heap priority queue to run in O(nlogn) where n is the characters with nonzero frequency
	 * @param freqTable the frequency table for characters 0..255 plus 256 = "end-of-file" with same specs are return value of buildFrequencyTable
	 * @return root of the Huffman tree build by this method
	 */

	private HuffmanTreeNode buildEncodingTree(ArrayList<Integer> freqTable) {
		
		// creates new huffman tree using a priority queue based on the frequency at the root
		HeapPriorityQueue<Integer,HuffmanTreeNode> P = new HeapPriorityQueue<Integer,HuffmanTreeNode>();

		for(int i=0;i<257;i++){ // for loop going throught the frequency table initlaizing the priorityqueue
			if(freqTable.get(i)!=0){
				P.insert(freqTable.get(i),new HuffmanTreeNode(i, freqTable.get(i), null, null)) ; // inserting as key the frequency and as value a Node containing frequency and character
			}
		}
		while(P.size()>1){

			HuffmanTreeNode e1 = P.removeMin().getValue();     	// removes Min from P
			HuffmanTreeNode e2 = P.removeMin().getValue();    	//removes Min from P
			
			HuffmanTreeNode T = new HuffmanTreeNode(-1, e1.count+e2.count, e1, e2); 

			P.insert(e1.count+e2.count, T); // inserts parent back into Priority queue
		}
		
		
	
	   return P.removeMin().getValue(); // return root of tree
	}

	
	/**
	 * 
	 * @param encodingTreeRoot - input parameter storing the root of the HUffman tree
	 * @return an ArrayList<String> of length 257 where code.get(i) returns a String of 0-1 correspoding to each character in a Huffman tree
	 *                                                  code.get(i) returns null if i is not a leaf of the Huffman tree
	 */
	
	private ArrayList<String> buildEncodingTable(HuffmanTreeNode encodingTreeRoot) {
		ArrayList<String> code= new ArrayList<String>(257); 
		for (int i=0;i<257;i++) code.add(i,null);
		
		HuffmanTreeNode p = encodingTreeRoot;
		String s = "";
		
		addCode(code,p,s); // calls recursive method below
		
		return code; // returns encoded table
	}

	// recursive method that goes through (similar to the preorder traversal method)
	void addCode(ArrayList<String> code,HuffmanTreeNode node,String s){
			
		if(node==null){ // base case checks if the left or right node is null and returns
			//s=s.substring(0, s.length()-1);
			return ;
		}	
		
		// if the node is a character add the encoded string to the array
		if(node.character!=-1){
			code.set(node.character ,s);
		}

		addCode(code, node.left, s+"0"); // recursivly calls with the left node and increase the binary represented number with 0
 
		addCode(code, node.right, s+"1"); // recursivly calls with the right node and increase the binary represented number with 1
	} 
	
	/**
	 * Encodes an input using encoding Table that stores the Huffman code for each character
	 * @param input - input parameter, a file to be encoded using Huffman encoding
	 * @param encodingTable - input parameter, a table containing the Huffman code for each character
	 * @param output - output paramter - file where the encoded bits will be written to.
	 * @throws IOException indicates I/O errors for input/output streams
	 */
	private void encodeData(InputStream input, ArrayList<String> encodingTable, OutputStream output) throws IOException {
		OutBitStream bitStream = new OutBitStream(output); // uses bitStream to output bit by bit
		
		
		int i = input.read(); // reads first byte
		int H=0;
		int L=0;
		while(i!=-1){

			String HuffCode = encodingTable.get(i); // gets the encoded huffman code of the character
			
			
			for(int k=0;k<HuffCode.length();k++){  // selects the encoded byte from table and writes it to ouput bit by bit	
				
				Character ShuffCode = HuffCode.charAt(k);  
				if(ShuffCode=='0'){
					bitStream.writeBit(0);
				}
				else if(ShuffCode=='1'){
					bitStream.writeBit(1);
				}
				if(bitStream.buffCount==7){
					L++;
				}
			}	 
			
			i=input.read(); // read next bye
			H++;
		}


		for(int k=0;k<encodingTable.get(256).length();k++){  // adds the last encoded special character at end of file
			Character ShuffCode =encodingTable.get(256).charAt(k);
			if(ShuffCode=='0'){
				bitStream.writeBit(0);
			}
			else if(ShuffCode=='1'){
				bitStream.writeBit(1);
			}
			if(bitStream.buffCount==7){
				L++;
			}
		}

		System.out.println("Number of bytes in input :"+H);
		System.out.println("Number of bytes in output :"+L);

		
		input.close();
		bitStream.close(); // close bit stream; flushing what is in the bit buffer to output file
	}
	
	/**
	 * Decodes an encoded input using encoding tree, writing decoded file to output
	 * @param input  input parameter a stream where header has already been read from
	 * @param encodingTreeRoot input parameter contains the root of the Huffman tree
	 * @param output output parameter where the decoded bytes will be written to 
	 * @throws IOException indicates I/O errors for input/output streams
	 */
	private void decodeData(ObjectInputStream input, HuffmanTreeNode encodingTreeRoot, FileOutputStream output) throws IOException {
		
		InBitStream inputBitStream= new InBitStream(input); // associates a bit stream to read bits from file
		
		HuffmanTreeNode p = encodingTreeRoot;
		
		int i = inputBitStream.readBit();
		
		int H=0;
		int L=0;

		while(inputBitStream.buffer!=-1){
			
				if(i==0){
					p=p.left;  // go left if the bit read is 0
					if(p==null){
						return;
					}

					if(p.character!=-1){	// if its a character write it to the file
						if(p.character==256){  //if special character is found stop the process
							break;
						}
						
						output.write(p.character);
						L++;
						p=encodingTreeRoot;
						
					}
				}
				else if(i==1){
					p=p.right;  // go right if the bit read is 1
					
					
					if(p==null){
						return;
					}
					if(p.character!=-1){	// if its a character write it to the file
						if(p.character==256){ //if special character is found stop the process
							break;
						}
						
						output.write(p.character);
						L++;
						p=encodingTreeRoot;
						
					}
				}
				
				i = inputBitStream.readBit();
				if(inputBitStream.buffCount==8){
					H++;
				}
			
		}
		System.out.println("Number of bytes in input :"+H);
		System.out.println("Number of bytes in output :"+L);
		
		input.close();
		output.close(); // close bit stream; flushing what is in the bit buffer to output file
		
    }
	
	/**
	 * Method that implements Huffman encoding on plain input into encoded output
	 * @param input - this is the file to be encoded (compressed)
	 * @param codedOutput - this is the Huffman encoded file corresponding to input
	 * @throws IOException indicates problems with input/output streams
	 */
	public void encode(String inputFileName, String outputFileName) throws IOException {
		System.out.println("\nEncoding "+inputFileName+ " " + outputFileName);
		
		// prepare input and output files streams
		FileInputStream input = new FileInputStream(inputFileName);
		FileInputStream copyinput = new FileInputStream(inputFileName); // create copy to read input twice
		FileOutputStream out = new FileOutputStream(outputFileName);
 		ObjectOutputStream codedOutput= new ObjectOutputStream(out); // use ObjectOutputStream to print objects to file
 		
		ArrayList<Integer> freqTable= buildFrequencyTable(input); // build frequencies from input
		//System.out.println("FrequencyTable is="+freqTable);
		HuffmanTreeNode root= buildEncodingTree(freqTable); // build tree using frequencies
		ArrayList<String> codes= buildEncodingTable(root);  // buildcodes for each character in file
		//System.out.println("EncodingTable is="+codes);
		codedOutput.writeObject(freqTable); //write header with frequency table
		encodeData(copyinput,codes,codedOutput); // write the Huffman encoding of each character in file
	}
	
    /**
     * Method that implements Huffman decoding on encoded input into a plain output
     * @param codedInput  - this is an file encoded (compressed) via the encode algorithm of this class 
     * @param output      - this is the output where we must write the decoded file  (should original encoded file)
     * @throws IOException - indicates problems with input/output streams
     * @throws ClassNotFoundException - handles case where the file does not contain correct object at header
     */
	public void decode (String inputFileName, String outputFileName) throws IOException, ClassNotFoundException {
		System.out.println("\nDecoding "+inputFileName+ " " + outputFileName);
		// prepare input and output file streams
		FileInputStream in = new FileInputStream(inputFileName);
 		ObjectInputStream codedInput= new ObjectInputStream(in);
 		FileOutputStream output = new FileOutputStream(outputFileName);
 		
		ArrayList<Integer> freqTable = (ArrayList<Integer>) codedInput.readObject(); //read header with frequency table
		//System.out.println("FrequencyTable is="+freqTable);
		HuffmanTreeNode root= buildEncodingTree(freqTable);
		decodeData(codedInput, root, output);
	}
	
	
}
	
    