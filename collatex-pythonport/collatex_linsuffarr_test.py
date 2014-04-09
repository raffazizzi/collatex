'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from linsuffarr import SuffixArray
from collatex_simple import SuperMaximumRe, Block
from rangeset import RangeSet




    
    
    



class Test(unittest.TestCase):


    #TODO: write asserts
    def testLineairSuffixArray(self):
        sa = SuffixArray("The Quick Brown Fox Jumped Over")
        print(sa)
        pass

    #TODO: write asserts
    def testSA2(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        print(sa)
        print(sa._LCP_values)
        
    def test_blocks(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        smr = SuperMaximumRe()
        blocks = smr.find_blocks(sa)
        # we expect two blocks ("a b c d F g h i", "! q r s t")
        #TODO: now the first block has size 9 instead of 8!
        #NOTE: It at the moment finds the Maximum Repeats not the Super Maximal Repeats
        block1 = Block(RangeSet(0,8).union(RangeSet(16,24)))
        block2 = Block(RangeSet(10,14).union(RangeSet(24,28)))
        #print(blocks)
        self.assertEqual([block1, block2], blocks)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()