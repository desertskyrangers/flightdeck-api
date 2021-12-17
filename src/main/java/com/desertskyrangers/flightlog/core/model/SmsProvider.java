package com.desertskyrangers.flightlog.core.model;

public enum SmsProvider {
	ATT("txt.att.net","mms.att.net"),
	SPRINT("messaging.sprintpcs.com","pm.sprint.com"),
	TMOBILE("tmomail.net","tmomail.net"),
	VERIZON("vtext.com","vzwpix.com");

	private String smsGateway;

	private String mmsGateway;

	SmsProvider( String smsGateway, String mmsGateway ) {
		this.smsGateway = smsGateway;
		this.mmsGateway = mmsGateway;
	}

	public String getSmsGateway() {
		return smsGateway;
	}

	public String getMmsGateway() {
		return mmsGateway;
	}

}
