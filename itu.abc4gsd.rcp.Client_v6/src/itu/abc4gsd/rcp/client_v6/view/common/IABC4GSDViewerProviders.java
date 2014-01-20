package itu.abc4gsd.rcp.client_v6.view.common;

import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

public interface IABC4GSDViewerProviders {
	void operationAdd( IABC4GSDItem element );
	void operationEdit( IABC4GSDItem element );
	void operationDelete( IABC4GSDItem element );
}
