package uk.co.badgersinfoil.chunkymonkey.h264;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import uk.co.badgersinfoil.chunkymonkey.h264.H264PesConsumer.ParseState;
import uk.co.badgersinfoil.chunkymonkey.ts.ElementryContext;
import uk.co.badgersinfoil.chunkymonkey.ts.PESPacket;

public class H264Context implements ElementryContext {
	private boolean ignoreRest;
	private PESPacket pesPacket;
	private int unitIndex = 0;
	private SeqParamSet lastSeqParamSet;
	private boolean nalStarted;
	private NALUnit nalUnit;
	private ParseState parseState;
	private ByteBuf seiBuffer = Unpooled.buffer();
	private ByteBuf seqParamSetBuffer = Unpooled.buffer();
	private NalUnitConsumer consumer;

	public boolean isIgnoreRest() {
		return ignoreRest;
	}
	public void setIgnoreRest(boolean ignoreRest) {
		this.ignoreRest = ignoreRest;
	}
	public PESPacket getPesPacket() {
		return pesPacket;
	}
	public void setPesPacket(PESPacket pesPacket) {
		this.pesPacket = pesPacket;
	}
	public int nextUnitIndex() {
		return unitIndex++;
	}
	public void start() {
		parseState = ParseState.START;
		ignoreRest = false;
		unitIndex = 0;
		nalStarted = false;
	}
	public void lastSeqParamSet(SeqParamSet params) {
		this.lastSeqParamSet = params;
	}
	public SeqParamSet lastSeqParamSet() {
		return lastSeqParamSet;
	}
	public boolean nalStarted() {
		return nalStarted;
	}
	public void nalStarted(boolean b) {
		nalStarted = b;
	}
	public void setNalUnit(NALUnit u) {
		nalUnit = u;
	}
	public NALUnit getNalUnit() {
		return nalUnit;
	}
	public ParseState state() {
		return parseState;
	}
	public void state(ParseState state) {
		parseState = state;
	}
	public ByteBuf seiBuffer() {
		return seiBuffer ;
	}
	public ByteBuf seqParamSetBuffer() {
		return seqParamSetBuffer;
	}
	public void setNalUnitConsumer(NalUnitConsumer consumer) {
		this.consumer = consumer;
	}
	public NalUnitConsumer getNalUnitConsumer() {
		return consumer;
	}
}
