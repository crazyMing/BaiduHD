package com.baidu.hd.debug;

import java.util.ArrayList;
import java.util.List;

import com.baidu.hd.module.P2PBlock;

class P2PShowBlock {
	
	public static final int None = 0;
	public static final int Downloading = 1;
	public static final int Complete = 2;
	
	private List<Section> mSections = new ArrayList<Section>();

	public Section getSection(int index) {
		return this.mSections.get(index);
	}
	
	public int getSectionCount() {
		return this.mSections.size();
	}
	
	public void setBlock(long totalSize, P2PBlock block) {
		
		this.mSections.clear();
		
		if(block == null) {
			return;
		}
		
		// 总段数
		long totalSectionCount = totalSize / block.getSectionSize();
		if(totalSize % block.getSectionSize() != 0) {
			totalSectionCount += 1;
		}
		
		// 总块数
		long totalBlockCount = totalSize / block.getUnitByte();
		if(totalSize / block.getUnitByte() != 0) {
			totalBlockCount += 1;
		}
		
		// 每段的块数
		long blockCountPerSection = block.getSectionSize() / block.getUnitByte();
		
		long beginBlockIndex = 0;
		for(int i = 0 ; i < totalSectionCount; ++i) {
			
			// 闭区间，所以 -1
			long endBlockIndex = beginBlockIndex + blockCountPerSection - 1;
			if(endBlockIndex > totalBlockCount - 1) {
				endBlockIndex = totalBlockCount - 1;
			}
			this.mSections.add(new Section(beginBlockIndex, endBlockIndex));
			beginBlockIndex += blockCountPerSection;
		}
		
		for(P2PBlock.Pair pair: block.getPairs()) {
			for(Section totalBlock: this.mSections) {
				totalBlock.fetchBlockToSelf(pair.getBegin(), pair.getEnd());
			}
		}
	}
	
	class Section {
		
		private long mBegin = 0;
		private long mEnd = 0;

		private List<Block> mBlocks = new ArrayList<Block>();
		
		private int state = None;

		public Section(long begin, long end) {
			this.mBegin = begin;
			this.mEnd = end;
		}
		
		public int getState() {
			return this.state;
		}
		
		public long getRange() {
			return this.mEnd - this.mBegin;
		}
		
		public List<Block> getBlocks() {
			return this.mBlocks;
		}
		
		public void fetchBlockToSelf(long begin, long end) {
			
			if(begin > this.mEnd || end < this.mBegin) {
				return;
			}
			
			long selfBegin = 0, selfEnd = 0;
			if(begin < this.mBegin) {
				selfBegin = this.mBegin;
			} else {
				selfBegin = begin;
			}
			if(end > this.mEnd) {
				selfEnd = this.mEnd;
			} else {
				selfEnd = end;
			}
			
			// 取相对值，不是绝对值
			selfBegin -= this.mBegin;
			selfEnd -= this.mBegin;
			this.mBlocks.add(new Block(selfBegin, selfEnd));
			
			if(selfEnd - selfBegin == this.getRange()) {
				this.state = Complete;
			} else {
				this.state = Downloading;
			}
		}

		class Block {
			
			private long begin = 0;
			private long end = 0;
			
			public Block(long begin, long end) {
				this.begin = begin;
				this.end = end;
			}
			public long getBegin() {
				return begin;
			}
			public long getEnd() {
				return end;
			}
			public long getRange() {
				return this.end - this.begin;
			}
		}
	}
}


















