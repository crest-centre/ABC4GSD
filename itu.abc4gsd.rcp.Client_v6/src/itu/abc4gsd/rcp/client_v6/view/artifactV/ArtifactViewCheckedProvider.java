package itu.abc4gsd.rcp.client_v6.view.artifactV;

 import itu.abc4gsd.rcp.client_v6.view.model.IABC4GSDItem;

import org.eclipse.jface.viewers.ICheckStateProvider;


public class ArtifactViewCheckedProvider implements ICheckStateProvider {

	@Override
	public boolean isChecked(Object obj) {
		if (obj instanceof IABC4GSDItem)
			return ((IABC4GSDItem) obj).get("autoLoad").toString().toLowerCase().equals("true") ;
		return false;
		}

	@Override
	public boolean isGrayed(Object element) {
		return false;
	}

}

