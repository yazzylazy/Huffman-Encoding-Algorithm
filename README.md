# Huffman-Encoding-Algorithm
Huffman Encoding Algorithm using binary trees to store the Huffman tree and it will use priority queues as part of the greedy algorithm that builds the Huffman tree.


Essentially the huffman encoding is composed of 2 steps. Compression and Decompression.

##  Compression

I seperated the compression in 5 steps:

  1.I read the input file once and compute letter frequencies

  2.I built the Huffman tree based on the letter frequency
  
  3.I computed the Huffman encoding for each letter
  
  4.I wrote the header of the output file which must contain the frequency table.
  
  5.I read input file a second time, converting each letter/byte into its Huffman encoding and sending these bits to output file.


##  Decompresion

I seperated the decompression in two steps 

  1.I read the letter frequencies from the input file header and rebuild the Huffman tree.
  
  2.I read the remaining bits of the input files producing the decoded file.

![](huff.gif)
