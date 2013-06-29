package com.baidu.hd.module;

import java.util.ArrayList;
import java.util.List;

/**
 * 两种单位：块，段
 * 块是最小单位
 * 段是大一级的单位
 */
public class P2PBlock {

	/**
	 * 已下载的块对
	 */
	public static class Pair {
		
		/**
		 * 起始块索引
		 */
		private long begin = 0;
		
		/**
		 * 结束块索引
		 */
		private long end = 0;
		public Pair(long begin, long end) {
			this.begin = begin;
			this.end = end;
		}
		public long getBegin() {
			return begin;
		}
		public long getEnd() {
			return end;
		}
	}
	
	/**
	 * 最合适的段大小
	 */
	private int sectionSize = 0;

	/**
	 * 每块的大小
	 */
	private int unitByte = 0;
	
	/**
	 * 已下载完成的块对的集合
	 */
	private List<Pair> pairs = new ArrayList<Pair>();
	
	public int getUnitByte() {
		return unitByte;
	}
	public void setUnitByte(int unitByte) {
		this.unitByte = unitByte;
	}
	public int getSectionSize() {
		return sectionSize;
	}
	public void setSectionSize(int sectionSize) {
		this.sectionSize = sectionSize;
	}
	public List<Pair> getPairs() {
		return pairs;
	}
	public void addPair(Pair pair) {
		this.pairs.add(pair);
	}
}
