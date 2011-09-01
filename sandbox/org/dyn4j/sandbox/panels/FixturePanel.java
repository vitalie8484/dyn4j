package org.dyn4j.sandbox.panels;

import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.sandbox.controls.JSliderWithTextField;
import org.dyn4j.sandbox.listeners.SelectTextFocusListener;

/**
 * Panel used to edit a fixture.
 * @author William Bittle
 * @version 1.0.0
 * @since 1.0.0
 */
public class FixturePanel extends WindowSpawningPanel implements InputPanel {
	/** The version id */
	private static final long serialVersionUID = 93686595772420446L;

	/**
	 * Class used to display the categories in a JList.
	 * @author William Bittle
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	private static class Category {
		/** The value of the category */
		public int value;
		
		/** The text shown in the list box */
		public String text;
		
		/**
		 * Full constructor.
		 * @param value the category
		 * @param text the category name
		 */
		public Category(int value, String text) {
			this.value = value;
			this.text = text;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.text;
		}
	}
	
	/**
	 * Generates the list of available categories.
	 */
	static {
		// the list of categories
		Category[] categories = new Category[32];
		// add the initial ones
		categories[0] = new Category(Integer.MAX_VALUE, "All");
		int v = 1;
		for (int i = 1; i < 32; i++) {
			categories[i] = new Category(v, "Category " + i);
			v *= 2;
		}
		CATEGORIES = categories;
	}
	
	// name
	
	/** The fixture name label */
	private JLabel lblName;
	
	/** The fixture name text box */
	private JTextField txtName;
	
	// categories
	
	/** The list of available categories */
	private static final Category[] CATEGORIES;
	
	/** The fixture being edited */
	private BodyFixture bodyFixture;
	
	// density
	
	/** The density label */
	private JLabel lblDensity;
	
	/** The density input text box */
	private JFormattedTextField txtDensity;
	
	// restitution
	
	/** The restitution label */
	private JLabel lblRestitution;
	
	/** The restitution slider */
	private JSliderWithTextField sldRestitution;
	
	// friction
	
	/** The friction label */
	private JLabel lblFriction;
	
	/** The friction slider */
	private JSliderWithTextField sldFriction;
	
	// filter
	
	/** The filter label */
	private JLabel lblFilter;
	
	/** The default filter radio button */
	private JRadioButton rdoDefaultFilter;
	
	/** The category filter radio button */
	private JRadioButton rdoCategoryFilter;
	
	/** The categories list label */
	private JLabel lblCategories;
	
	/** The masks list label */
	private JLabel lblMasks;
	
	/** The list of categories */
	private JList lstCategories;
	
	/** The list of masks */
	private JList lstMasks;
	
	/** The scroll pane for the categories list */
	private JScrollPane scrCategories;
	
	/** The scroll pane for the masks list */
	private JScrollPane scrMasks;
	
	// sensor
	
	/** The sensor label */
	private JLabel lblSensor;
	
	/** The sensor checkbox */
	private JCheckBox chkSensor;
	
	/**
	 * Full constructor.
	 * @param parent the parent window, frame or dialog
	 * @param fixture the fixture to edit
	 */
	public FixturePanel(Window parent, BodyFixture fixture) {
		super(parent);
		this.bodyFixture = fixture;
		
		// see if the name is already populated
		String name = (String)fixture.getUserData();
		
		this.lblName = new JLabel("Name");
		this.txtName = new JTextField(name);
		this.txtName.addFocusListener(new SelectTextFocusListener(this.txtName));
		this.txtName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				handleEvent();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				handleEvent();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {}
			/**
			 * Handles the event by saving the text.
			 */
			private void handleEvent() {
				Object value = txtName.getText();
				bodyFixture.setUserData(value);
			}
		});
		
		this.lblDensity = new JLabel("Density");
		this.lblDensity.setToolTipText("<html>The density in Kilograms/Meter<sup>2</sup></html>");
		this.txtDensity = new JFormattedTextField(new DecimalFormat("0.00"));
		this.txtDensity.setToolTipText("<html>The density in Kilograms/Meter<sup>2</sup></html>");
		this.txtDensity.setValue(fixture.getDensity());
		this.txtDensity.setColumns(4);
		this.txtDensity.setMaximumSize(this.txtDensity.getPreferredSize());
		this.txtDensity.addFocusListener(new SelectTextFocusListener(this.txtDensity));
		this.txtDensity.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				JFormattedTextField field = (JFormattedTextField)evt.getSource();
				Number number = (Number)field.getValue();
				double value = number.doubleValue();
				if (value <= 0.0) {
					value = 0.01;
					field.setValue(value);
				}
				bodyFixture.setDensity(value);
			}
		});
		
		this.lblRestitution = new JLabel("Restitution");
		this.lblRestitution.setToolTipText(
				"<html>The restitution value determines the amount of energy retained after a collision." +
				"<br />Valid values are between 0 to infinity." +
				"<br />Larger values will decrease the amount of lost energy.</html>");
		this.sldRestitution = new JSliderWithTextField(0, 100, (int)(fixture.getRestitution() * 100.0), 0.01, new DecimalFormat("0.00"));
		this.sldRestitution.setToolTipText(
				"<html>The restitution value determines the amount of energy retained after a collision." +
				"<br />Valid values are between 0 to infinity." +
				"<br />Larger values will decrease the amount of lost energy.</html>");
		this.sldRestitution.setColumns(4);
		this.sldRestitution.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSliderWithTextField field = (JSliderWithTextField)e.getSource();
				double value = field.getScaledValue();
				bodyFixture.setRestitution(value);
			}
		});
		
		this.lblFriction = new JLabel("Friction");
		this.lblFriction.setToolTipText(
				"<html>The friction coefficient determines the roughness of a fixture's surface." +
				"<br />This allows bodies sliding across this fixture to slow down." +
				"<br />Valid values are between 0 to infinity." +
				"<br />Larger values will increase the rate at which bodies are slowed.</html>");
		this.sldFriction = new JSliderWithTextField(0, 100, (int)(fixture.getRestitution() * 100.0), 0.01, new DecimalFormat("0.00"));
		this.sldFriction.setToolTipText(
				"<html>The friction coefficient determines the roughness of a fixture's surface." +
				"<br />This allows bodies sliding across this fixture to slow down." +
				"<br />Valid values are between 0 to infinity." +
				"<br />Larger values will increase the rate at which bodies are slowed.</html>");
		this.sldFriction.setColumns(4);
		this.sldFriction.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSliderWithTextField field = (JSliderWithTextField)e.getSource();
				double value = field.getScaledValue();
				bodyFixture.setFriction(value);
			}
		});
		
		this.lblFilter = new JLabel("Filter");
		this.lblFilter.setToolTipText("The filter allows certain groups of fixtures to collide or not collide.");
		this.rdoDefaultFilter = new JRadioButton("Default");
		this.rdoDefaultFilter.setToolTipText("The default filter allows this fixture to collide with all other fixtures.");
		this.rdoCategoryFilter = new JRadioButton("Category");
		this.lblCategories = new JLabel("Groups");
		this.lblMasks = new JLabel("Masks");
		this.lstCategories = new JList(CATEGORIES);
		this.lstMasks = new JList(CATEGORIES);
		
		this.scrCategories = new JScrollPane(this.lstCategories);
		this.scrMasks = new JScrollPane(this.lstMasks);
		
		this.scrCategories.setPreferredSize(new Dimension(150, 200));
		this.scrMasks.setPreferredSize(new Dimension(150, 200));
		this.scrCategories.setMinimumSize(this.scrCategories.getPreferredSize());
		this.scrMasks.setMinimumSize(this.scrMasks.getPreferredSize());
		
		this.rdoCategoryFilter.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent event) {
				JRadioButton radio = (JRadioButton)event.getSource();
				if (radio.isSelected()) {
					lblCategories.setVisible(true);
					scrCategories.setVisible(true);
					lblMasks.setVisible(true);
					scrMasks.setVisible(true);
				} else {
					lblCategories.setVisible(false);
					scrCategories.setVisible(false);
					lblMasks.setVisible(false);
					scrMasks.setVisible(false);
				}
			}
		});
		if (fixture.getFilter() == Filter.DEFAULT_FILTER) {
			this.rdoDefaultFilter.setSelected(true);
			lblCategories.setVisible(false);
			scrCategories.setVisible(false);
			lblMasks.setVisible(false);
			scrMasks.setVisible(false);
			
			this.lstCategories.setSelectedIndex(0);
			this.lstMasks.setSelectedIndex(0);
		} else {
			this.rdoCategoryFilter.setSelected(true);
			lblCategories.setVisible(true);
			scrCategories.setVisible(true);
			lblMasks.setVisible(true);
			scrMasks.setVisible(true);
			
			// set the default selected groups
			CategoryFilter filter = (CategoryFilter)fixture.getFilter();
			int category = filter.getCategory();
			int mask = filter.getMask();
			
			int[] indices = this.getSelectedIndices(category);
			this.lstCategories.setSelectedIndices(indices);
			indices = this.getSelectedIndices(mask);
			this.lstMasks.setSelectedIndices(indices);
		}
		ButtonGroup bg = new ButtonGroup();
		bg.add(this.rdoDefaultFilter);
		bg.add(this.rdoCategoryFilter);
		
		this.lstCategories.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// make sure the user is done adjusting
				if (!e.getValueIsAdjusting()) {
					JList list = (JList)e.getSource();
					int[] selections = list.getSelectedIndices();
					int value = 0;
					for (int i = 0; i < selections.length; i++) {
						value |= CATEGORIES[selections[i]].value;
					}
					updateFixutreFilterCategory(value);
				}
			}
		});
		
		this.lstMasks.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// make sure the user is done adjusting
				if (!e.getValueIsAdjusting()) {
					JList list = (JList)e.getSource();
					int[] selections = list.getSelectedIndices();
					int value = 0;
					for (int i = 0; i < selections.length; i++) {
						value |= CATEGORIES[selections[i]].value;
					}
					updateFixutreFilterMask(value);
				}
			}
		});
		
		this.lblSensor = new JLabel("Is Sensor");
		this.lblSensor.setToolTipText("A sensor fixture is a fixture that will be detected during collision but not resolved.");
		this.chkSensor = new JCheckBox();
		this.chkSensor.setToolTipText("A sensor fixture is a fixture that will be detected during collision but not resolved.");
		this.chkSensor.setSelected(fixture.isSensor());
		this.chkSensor.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JCheckBox check = (JCheckBox)e.getSource();
				bodyFixture.setSensor(check.isSelected());
			}
		});
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		layout.setHonorsVisibility(true);
		this.setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblName)
						.addComponent(this.lblFilter)
						.addComponent(this.lblSensor)
						.addComponent(this.lblDensity)
						.addComponent(this.lblFriction)
						.addComponent(this.lblRestitution))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.txtName)
						.addGroup(
								layout.createSequentialGroup()
								.addComponent(this.rdoDefaultFilter)
								.addComponent(this.rdoCategoryFilter))
						.addGroup(
								layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup()
										.addComponent(this.lblCategories)
										.addComponent(scrCategories))
								.addGroup(
										layout.createParallelGroup()
										.addComponent(this.lblMasks)
										.addComponent(scrMasks)))
						.addComponent(this.chkSensor)
						.addComponent(this.txtDensity)
						.addComponent(this.sldFriction)
						.addComponent(this.sldRestitution)));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblName)
						.addComponent(this.txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblFilter)
						.addComponent(this.rdoDefaultFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(this.rdoCategoryFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(
						layout.createParallelGroup()
						.addGroup(
								layout.createSequentialGroup()
								.addComponent(this.lblCategories)
								.addComponent(scrCategories, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(
								layout.createSequentialGroup()
								.addComponent(this.lblMasks)
								.addComponent(scrMasks, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblSensor)
						.addComponent(this.chkSensor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblDensity)
						.addComponent(this.txtDensity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblFriction)
						.addComponent(this.sldFriction, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(
						layout.createParallelGroup()
						.addComponent(this.lblRestitution)
						.addComponent(this.sldRestitution, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.InputPanel#isValidInput()
	 */
	@Override
	public boolean isValidInput() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.dyn4j.sandbox.panels.InputPanel#showInvalidInputMessage(java.awt.Window)
	 */
	@Override
	public void showInvalidInputMessage(Window owner) {}
	
	/**
	 * Updates the fixture's filter with the respective category value.
	 * @param category the category (can be multiple categories)
	 */
	private void updateFixutreFilterCategory(int category) {
		Filter filter = bodyFixture.getFilter();
		if (filter == Filter.DEFAULT_FILTER) {
			CategoryFilter cf = new CategoryFilter(category, Integer.MAX_VALUE);
			bodyFixture.setFilter(cf);
		} else {
			CategoryFilter cf = (CategoryFilter)filter;
			cf.setCategory(category);
		}
	}
	
	/**
	 * Updates the fixture's filter with the respective mask value.
	 * @param mask the category mask
	 */
	private void updateFixutreFilterMask(int mask) {
		Filter filter = bodyFixture.getFilter();
		if (filter == Filter.DEFAULT_FILTER) {
			CategoryFilter cf = new CategoryFilter(Integer.MAX_VALUE, mask);
			bodyFixture.setFilter(cf);
		} else {
			CategoryFilter cf = (CategoryFilter)filter;
			cf.setMask(mask);
		}
	}
	
	/**
	 * Returns an array of indices that should be selected in the JList
	 * given the value of the category or mask.
	 * @param value the category or mask
	 * @return int[] the selected indices
	 */
	private int[] getSelectedIndices(int value) {
		List<Integer> indexList = new ArrayList<Integer>();
		int t = 1;
		for (int i = 1; i < 32; i++) {
			if ((value & t) == t) {
				indexList.add(i);
			}
			t *= 2;
		}
		int[] indices = new int[indexList.size()];
		for (int i = 0; i < indexList.size(); i++) {
			indices[i] = indexList.get(i);
		}
		return indices;
	}
}