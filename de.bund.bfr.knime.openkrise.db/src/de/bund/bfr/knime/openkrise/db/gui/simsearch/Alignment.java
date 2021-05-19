/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.gui.simsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Alignment {
  
	public static enum EditOperation {
		None, GAP, // Operation for reference and non reference sequences 
		Replace, Insert, Delete,  //Operation for non reference sequences
	}
	
	// Class holds a sequence of characters (the text)
	// and the edit operation
	public static class AlignedSequence implements Comparable<AlignedSequence>{
	      private String sequence;
	      private List<Alignment.EditOperation> editOperations;
	      
	      public AlignedSequence(String sequence, List<Alignment.EditOperation> editOperations) {
	        this.sequence = sequence;
	        this.editOperations = editOperations;
	      }
	      
	      public AlignedSequence(String sequence) {
            this.sequence = sequence;
          }
	      
	      public String getSequence() { return this.sequence; }
	      public List<Alignment.EditOperation> getEditOperations() { return this.editOperations; }

	      @Override
	      public int compareTo(AlignedSequence o) {
	        if(this.sequence==null && (o==null || o.getSequence()==null)) return 0;
	        if(this.sequence==null) return -o.getSequence().compareTo(this.sequence);
	        return this.sequence.compareTo((o==null?null:o.getSequence()));
	      }
	      
	      @Override
	      public String toString() {
	        return (this.sequence==null?"":this.sequence);
	      }
	    }
	
	// global optimal aligns the referenceText to another text
	private static List<EditOperation> alignToReference(String referenceText, String alignText) {
	  
		if(referenceText==null || alignText==null) return null;
		if(referenceText.isEmpty() || alignText.isEmpty()) return new ArrayList<>();
		
		List<EditOperation> editOperations = new ArrayList<>();
		
		final char[] referenceSequence = referenceText.toCharArray();
		final char[] alignSequence = alignText.toCharArray();
		final int referenceSequenceLength = referenceText.length();
		final int alignSequenceLength = alignText.length();
		
		final int[][] editDistance = new int[referenceSequenceLength+1][alignSequenceLength+1];
		editDistance[0][0] = 0;
		
		for(int iRow= 1; iRow <= referenceSequenceLength; ++iRow ) editDistance[iRow][0] = iRow;
		for(int iColumn = 1; iColumn <= alignSequenceLength; ++iColumn ) editDistance[0][iColumn] = iColumn;
		
		for(int iRow = 1; iRow <= referenceSequenceLength; ++iRow) {
		  for(int iColumn = 1; iColumn <= alignSequenceLength; ++iColumn) {
		    int align = editDistance[iRow-1][iColumn-1] + (referenceSequence[iRow-1]==alignSequence[iColumn-1]?0:1);
		    int delete = editDistance[iRow-1][iColumn] + 1;
		    int insert = editDistance[iRow][iColumn-1] + 1;
		    
		    editDistance[iRow][iColumn] = (align <= insert ? (align <= delete ? align : delete) : (insert <= delete ? insert : delete));   
		  }
		}
		// TraceBack
		int iRow = referenceSequenceLength;
		int iColumn = alignSequenceLength;
		
		while(iRow > 0 || iColumn>0) {
		  int currentEditDistance = editDistance[iRow][iColumn];
		  if(iRow>0 && currentEditDistance == editDistance[iRow-1][iColumn]+1) {
		    editOperations.add(EditOperation.Insert);
		    --iRow;
		  } else if (currentEditDistance == editDistance[iRow][iColumn-1]+1) {
		    editOperations.add(EditOperation.Delete);
		    --iColumn;
		  } else if(referenceSequence[iRow-1]==alignSequence[iColumn-1]) {
		    editOperations.add(EditOperation.None);
		    --iRow;
		    --iColumn;
		  } else {
		    --iRow;
            --iColumn;
		    editOperations.add(EditOperation.Replace);
		  }
		}
		
		Collections.reverse(editOperations);
		return editOperations;
	}
	
	// aligns multiple sequences
	// First step: each sequence is global optimal aligned to the reference sequence
	// Second step: the alignments are aggregate to a global not necessarily optimal alignment
	public static AlignedSequence[] alignSequences(String[] sequences, int referenceIndex) {
		  if(sequences==null) return null;
		  if(sequences.length==0) return new AlignedSequence[0];
		  
		  AlignedSequence[] result = new AlignedSequence[sequences.length];
		  if(referenceIndex<0 || referenceIndex>=sequences.length || 
				  sequences[referenceIndex]==null || sequences[referenceIndex].isEmpty()) {
			  for(int i=0; i<sequences.length; ++i) result[i] = new AlignedSequence(sequences[i]);
			  return result;
		  }
		  
		  Map<Integer, List<EditOperation>> editOperationsAlign = new HashMap<>();
		  Map<Integer, List<EditOperation>> editOperationsReference = new HashMap<>();
		  
		  for(int iSeq = 0; iSeq < sequences.length; ++iSeq) {
		    if(iSeq != referenceIndex) {
		      if(sequences[iSeq]!=null && !sequences[iSeq].isEmpty()) {
    		      List<EditOperation> editOperations = alignToReference(sequences[referenceIndex],sequences[iSeq]);
    		      editOperationsAlign.put(iSeq, editOperations);
    		      editOperationsReference.put(iSeq, getReferenceOperations(editOperations));
		      }
		    }
		  }
		  if(editOperationsAlign.size()>1) mergeEditOperations(editOperationsAlign, editOperationsReference);
		  
		  if(editOperationsAlign.isEmpty()) {
		    for(int iSeq=0; iSeq<sequences.length; ++iSeq) result[iSeq] = new AlignedSequence(sequences[iSeq]);
		  } else {
		    editOperationsAlign.put(referenceIndex, editOperationsReference.get(editOperationsReference.keySet().toArray()[0]));
		    for(int iSeq=0; iSeq<sequences.length; ++iSeq) result[iSeq] = 
		        new AlignedSequence(sequences[iSeq],(editOperationsAlign.containsKey(iSeq)? editOperationsAlign.get(iSeq):new ArrayList<>()));
		  }
		  
		  return result;
	    }
	
	
	// merges lists of edit operations
	private static void mergeEditOperations(Map<Integer, List<EditOperation>> editOperationsAlign, Map<Integer, List<EditOperation>> editOperationsReference) {
	  final int seqCount = editOperationsAlign.size();
	  
	  int[] seqLength = new int[seqCount];
	  List<List<EditOperation>> alignOperations = new ArrayList<>(seqCount);
	  List<List<EditOperation>> referenceOperations = new ArrayList<>(seqCount);
	  for(int key : editOperationsAlign.keySet()) {
	    alignOperations.add(editOperationsAlign.get(key));
	    referenceOperations.add(editOperationsReference.get(key));
	  }
	  int maxLength = 0;
	  for(int iSeq = 0; iSeq < seqCount; ++iSeq) {
	    seqLength[iSeq] = alignOperations.get(iSeq).size();
	    maxLength = Math.max(maxLength, seqLength[iSeq]);
	  }
	  
      int column = 0;
      while(maxLength>column) {
        boolean isGapNeeded = false;
        for(int iSeq = 0; iSeq < seqCount; ++iSeq) {
          if(column>=seqLength[iSeq] || referenceOperations.get(iSeq).get(column)==EditOperation.GAP) {
            isGapNeeded = true;
            break;
          }
        }
        if(isGapNeeded) {
          for(int iSeq = 0; iSeq < seqCount; ++iSeq) {
            if(column>=seqLength[iSeq]) {
              referenceOperations.get(iSeq).add(EditOperation.GAP);
              alignOperations.get(iSeq).add(EditOperation.GAP);
              seqLength[iSeq]+=1;
            } else if(referenceOperations.get(iSeq).get(column)!=EditOperation.GAP) {
              referenceOperations.get(iSeq).add(column,EditOperation.GAP);
              alignOperations.get(iSeq).add(column,EditOperation.GAP);
              seqLength[iSeq]+=1;
              maxLength = Math.max(maxLength,  seqLength[iSeq]);
            }
          }
        }
        ++column;
      }
    }
	
	// converts a list of edit operations to a list of reference operations
	private static List<EditOperation> getReferenceOperations(List<EditOperation> editOperations) {
	  return editOperations.stream().map(o -> (Arrays.asList(EditOperation.Insert, EditOperation.None, EditOperation.Replace).contains(o) ? EditOperation.None : EditOperation.GAP)).collect(Collectors.toList());
	}

}
