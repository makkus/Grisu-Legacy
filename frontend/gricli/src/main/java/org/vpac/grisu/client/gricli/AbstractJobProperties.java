package org.vpac.grisu.client.gricli;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.NoSuchJobException;
import org.vpac.grisu.fs.model.MountPoint;

public abstract class AbstractJobProperties implements JobProperties {
	
	protected ServiceInterface serviceInterface = null;
	
	private String userExecutionHostFs = null;
	private String workingdir = null;
	private String absoluteJobDir = null;

	
	public String getUserExecutionHostFs() {
		
		if ( userExecutionHostFs == null ) {

		MountPoint mp = serviceInterface.getMountPointForUri(getAbsoluteJobDir());
		
			userExecutionHostFs = mp.getRootUrl();
		
		} 
		return userExecutionHostFs;
	}
	
	public String getAbsoluteJobDir() {
		
		if ( absoluteJobDir == null ) {
			
			if ( !StringUtils.isEmpty(getSubmissionLocation()) && !StringUtils.isEmpty(getVO()) ) {
				absoluteJobDir = serviceInterface.calculateAbsoluteJobDirectory(getJobname(), getSubmissionLocation(), getVO());
			} else {
				try {
					absoluteJobDir = serviceInterface.getJobDirectory(getJobname());
				} catch (NoSuchJobException e) {
					//TODO log output?
					return null;
				}
			}
		}
		return absoluteJobDir;
	}

	public String getWorkingDirectory() {
		
		if ( workingdir == null ) {
		
		int i = 1;
		if (getUserExecutionHostFs().endsWith("/")) {
			i = 2;
		}
			workingdir = getAbsoluteJobDir().substring(getUserExecutionHostFs().length() + i);
		} 
		return workingdir;
	}

}
