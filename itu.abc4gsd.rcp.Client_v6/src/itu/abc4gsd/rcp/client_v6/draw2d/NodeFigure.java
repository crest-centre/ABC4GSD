package itu.abc4gsd.rcp.client_v6.draw2d;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.graphics.Color;

 import itu.abc4gsd.rcp.client_v6.view.model.ABC4GSDGraphItem;

public class NodeFigure extends Figure {
	public class CompartmentFigureBorder extends AbstractBorder {
		@Override
		public Insets getInsets(IFigure figure) {
		      return new Insets(1,0,0,0);
		}
		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
		      graphics.drawLine(getPaintRectangle(figure, insets).getTopLeft(),
                      tempRect.getTopRight());
		}
	  }
	final private Color highlight = ColorConstants.yellow;
	final private Color background = ColorConstants.lightGray;
	final private Color foreground = ColorConstants.darkGray;
	final private Color line = ColorConstants.black;
	final private int selectedLineWidth = 3;
	final private int unselectedLineWidth = 1;
	private boolean activated = false;
	private boolean selected = false;

	public NodeFigure( ABC4GSDGraphItem obj ) {
		String tmp = "";
		if( obj.placeHolder ) return;

		// The layout
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager( layout );
		setBorder( new LineBorder( line, unselectedLineWidth ));
		setOpaque( true );
		setForegroundColor( foreground );
		setBackgroundColor( background );
		
		// Name
		tmp = obj.getLabel();
		Label label = new Label( tmp != null ? (tmp.length() > 20 ? tmp.substring(0, 20) : tmp) : "" );
		add( label );
		
		// Tags
//		label = new Label( obj.tags.toString() );
//		label.setLabelAlignment( PositionConstants.LEFT );
//		label.setBorder( new CompartmentFigureBorder() ); 
//		add(label);

//		// Description
//		tmp = obj.content.get("description").toString();
//		label = new Label( tmp != null ? (tmp.length() > 20 ? tmp.substring(0, 20) : tmp) : "" );
//		label.setLabelAlignment( PositionConstants.LEFT );
//		label.setBorder( new CompartmentFigureBorder() ); 
//		add(label);

		// Participants
		label = new Label( "Active: " + obj.content.get("onlineParticipant") );
		label.setLabelAlignment( PositionConstants.LEFT );
		label.setBorder( new CompartmentFigureBorder() );
//		label.setBorder( new MarginBorder( 0, 4, 0, 4 ));
		add(label);

		// The image
//		CompartmentFigure patternImageFigure = new CompartmentFigure();
//		label = new Label( EipDesignerImageStore.INSTANCE.getEipImage( eipNode.getEipType()));
//		label.setBorder( new MarginBorder( 0, 4, 0, 4 ));
//		patternImageFigure.add( label );

//		patternImageFigure.add( label );
//		add( patternImageFigure );
		
		setToolTip(new Label(obj.getLabel() + "\n----------\n" + obj.content.get("description").toString()));
	}

	/**
	 * Sets the node as selected.
	 */
	public void setSelected(boolean selected) {
		if (selected == isSelected())
			return;
		this.selected = selected;
		if( selected )
			setBorder( new LineBorder( line, selectedLineWidth ));
		else
			setBorder( new LineBorder( line, unselectedLineWidth ));
		repaint();
	}
	public boolean isSelected() { return selected; }
	/**
	 * Sets the node as active.
	 */
	public void setActive(boolean selected) {
		if (selected == isActive())
			return;
		this.activated = selected;
		if( activated )
			setBackgroundColor( highlight );
		else
			setBackgroundColor( background );
		repaint();
	}
	public boolean isActive() { return activated; }

	
}

