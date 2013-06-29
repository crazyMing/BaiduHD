package com.baidu.hd.module;

import java.util.ArrayList;
import java.util.List;

/**
 * ���ֵ�λ���飬��
 * ������С��λ
 * ���Ǵ�һ���ĵ�λ
 */
public class P2PBlock {

	/**
	 * �����صĿ��
	 */
	public static class Pair {
		
		/**
		 * ��ʼ������
		 */
		private long begin = 0;
		
		/**
		 * ����������
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
	 * ����ʵĶδ�С
	 */
	private int sectionSize = 0;

	/**
	 * ÿ��Ĵ�С
	 */
	private int unitByte = 0;
	
	/**
	 * ��������ɵĿ�Եļ���
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
