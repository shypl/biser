package org.shypl.biser.csi.client {
	import org.shypl.biser.io.BiserReader;
	import org.shypl.common.logging.Logger;

	public interface ResultHandlerHolder {
		function process(reader:BiserReader, logger:Logger):void;
	}
}
