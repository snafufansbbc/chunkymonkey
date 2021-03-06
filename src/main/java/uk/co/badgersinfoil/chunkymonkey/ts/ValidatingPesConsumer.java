package uk.co.badgersinfoil.chunkymonkey.ts;

import io.netty.buffer.ByteBuf;
import uk.co.badgersinfoil.chunkymonkey.MediaContext;
import uk.co.badgersinfoil.chunkymonkey.event.Locator;
import uk.co.badgersinfoil.chunkymonkey.event.Reporter;
import uk.co.badgersinfoil.chunkymonkey.ts.PESPacket.Parsed;
import uk.co.badgersinfoil.chunkymonkey.ts.PESPacket.Timestamp;

public class ValidatingPesConsumer implements PESConsumer {

	public static class ValidatingElementryContext implements ElementryContext {
		private MediaContext parentContext;
		private Timestamp lastDts;
		public Integer lastStreamId;

		public ValidatingElementryContext(MediaContext parentContext) {
			this.parentContext = parentContext;
		}

		@Override
		public Locator getLocator() {
			return parentContext.getLocator();
		}
	}

	private Reporter rep;

	public ValidatingPesConsumer(Reporter rep) {
		this.rep = rep;
	}

	@Override
	public void start(ElementryContext ctx, PESPacket pesPacket) {
		ValidatingElementryContext vCtx = (ValidatingElementryContext)ctx;
		if (pesPacket.packetStartCodePrefix() != 1) {
			rep.carp(vCtx.getLocator(), "start_code_prefix should be 0x1, got: ", pesPacket.packetStartCodePrefix());
		}
		if (vCtx.lastStreamId != null && vCtx.lastStreamId != pesPacket.streamId()) {
			rep.carp(vCtx.getLocator(), "stream_id changed: %d, was previously %d", pesPacket.streamId(), vCtx.lastStreamId);
		}
		vCtx.lastStreamId = pesPacket.streamId();
		if (pesPacket.isParsed()) {
			Parsed payload = pesPacket.getParsedPESPaload();
			if (payload.ptsDdsFlags().isDtsPresent()) {
				if (vCtx.lastDts != null && vCtx.lastDts.isValid() && payload.dts().isValid()) {
					long diff = payload.dts().getTs() - vCtx.lastDts.getTs();
					if (diff < 0 && !isWrapLikely(diff)) {
						rep.carp(vCtx.getLocator(), "DTS went backwards: %s -> %s", vCtx.lastDts, payload.dts());
					} else if (diff == 0) {
						rep.carp(vCtx.getLocator(), "DTS failed to advance: %s", payload.dts());
					}
				}
				vCtx.lastDts = payload.dts();
			}
		}
	}

	private boolean isWrapLikely(long dtsDiff) {
		final long ONE_SECOND = 90_000;  // DTS @ 90kHz
		final int DTS_BITS = 33;
		return dtsDiff < 0 && (ONE_SECOND - 1L<<DTS_BITS) > dtsDiff;
	}

	@Override
	public void continuation(ElementryContext ctx, TSPacket packet, ByteBuf payload) {
		// TODO Auto-generated method stub

	}
	@Override
	public void end(ElementryContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void continuityError(ElementryContext ctx) {
		// TODO: in order to use Reporter instance, we need a Locator
		//       object to describe where the discontinuity occurred,
		//       but our API does not yet define this
	}

	@Override
	public ElementryContext createContext(MediaContext parentContext) {
		return new ValidatingElementryContext(parentContext);
	}
}
