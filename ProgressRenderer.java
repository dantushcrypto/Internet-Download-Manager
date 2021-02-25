import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

//this class renders a JProgressBar in a table cell
class ProgressRenderer extends JProgressBar implements TableCellRenderer {
    //constructor for ProgressRender
    public ProgressRenderer(int min, int max){
        super(min, max);
    }
    //returns this JProgressBar as the renderer for the given table cell

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        //set JProgressBar's percent complete value
        setValue((int) ((Float) value).floatValue());
        return this;
    }
}
