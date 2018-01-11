package org.asura.task.handler.rchandler;

import org.asura.core.data.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultResChainHandler extends ResChainHandler {

	protected Logger logger=LoggerFactory.getLogger(this.getClass());
	
	protected boolean isContinue = true;

	public boolean isContinue() {
		return isContinue;
	}

	public void setContinue(boolean isContinue) {
		this.isContinue = isContinue;
	}

	@Override
	public DataRecord process(DataRecord record, ResChainHandlerLog log) {
		DataRecord rd = record;
		try {
			rd = handle(record, log);
		} catch (Exception e) {
			log.log(e.getMessage());
			logger.error("exception occurred when process handler "+getName()+record.toString(), e);
		}
		if (isContinue() && getSuccesor() != null) {
			return getSuccesor().process(rd, log);
		}
		return rd;
	}

	public abstract DataRecord handle(DataRecord record, ResChainHandlerLog log) throws Exception;

}
