package org.vpac.grisu.client.view.grisuclient;

import org.apache.commons.cli.CommandLine;

public interface GrisuClientProperties {

	public abstract String getStageoutDirectory();

	public abstract String getServiceInterfaceUrl();

	public abstract String getMode();

	public abstract boolean stageOutResults();

	public abstract boolean cleanAfterStageOut();

	public abstract boolean verbose();
	
	public abstract boolean debug();

	public abstract boolean killPossiblyExistingJob();

	public abstract int getRecheckInterval();

	public abstract String getMyProxyUsername();

}