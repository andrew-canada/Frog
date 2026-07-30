package view;

import entity.Washroom;
import interface_adapter.directions.MapViewModel;
import interface_adapter.view_reviews.WashroomListViewModel;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public final class MainView extends JPanel{
    private final JPanel list=new JPanel(); private final JLabel routeLabel=Theme.label("Select a washroom to explore",13,Theme.MUTED);
    private final CampusMapPanel map=new CampusMapPanel(); private String selectedId="bahen-2";
    private Consumer<String> onReviews=id->{}; private Consumer<String> onDirections=id->{}; private Runnable onLogin=()->{},onRecommend=()->{},onReport=()->{},onBusyness=()->{}, onAccount=()->{};
    private double latitude=43.6629,longitude=-79.3957;
    public MainView(WashroomListViewModel washrooms,MapViewModel route){setLayout(new BorderLayout());setBackground(Theme.PAPER);
        add(header(),BorderLayout.NORTH);add(sidebar(),BorderLayout.WEST);add(mapArea(),BorderLayout.CENTER);
        washrooms.addPropertyChangeListener(e->renderList(washrooms.getState().items()));route.addPropertyChangeListener(e->{MapViewModel.State s=route.getState();
            if(s.success()){routeLabel.setText("<html><b>"+s.distance()+"</b> · about "+s.duration()+" walk</html>");map.setRouteVisible(true);}});}
    private JComponent header(){JPanel p=new JPanel(new BorderLayout());p.setBackground(Theme.PAPER);p.setBorder(Theme.pad(10,18,10,18));
        JLabel brand=Theme.label("FlushID",20,Theme.BLUE);brand.setFont(brand.getFont().deriveFont(Font.BOLD));p.add(brand,BorderLayout.WEST);
        JPanel nav=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));nav.setOpaque(false);for(JButton b:new JButton[]{nav("Recommend",()->onRecommend.run()),nav("Account",()->onAccount.run()),nav("Report status",()->onReport.run()),nav("Busyness",()->onBusyness.run()),nav("Login",()->onLogin.run())})nav.add(b);p.add(nav,BorderLayout.EAST);return p;}
    private JButton nav(String text,Runnable action){JButton b=Theme.button(text);b.addActionListener(e->action.run());return b;}
    private JComponent sidebar(){JPanel p=new JPanel(new BorderLayout(0,10));p.setPreferredSize(new Dimension(290,0));p.setBackground(Theme.PAPER);p.setBorder(Theme.pad(14,18,14,12));
        JPanel controls=new JPanel(new GridLayout(2,2,8,8));controls.setOpaque(false);JButton location=Theme.button("Location"),filters=Theme.button("Filters");controls.add(location);controls.add(filters);controls.add(new JLabel("Sort by:"));controls.add(new SortDropdownControl());p.add(controls,BorderLayout.NORTH);
        location.addActionListener(e->new LocationInputDialog(SwingUtilities.getWindowAncestor(this),(lat,lng)->{latitude=lat;longitude=lng;routeLabel.setText("Location updated — choose directions");}).setVisible(true));
        filters.addActionListener(e->JOptionPane.showMessageDialog(this,new FilterPanel(),"Filters",JOptionPane.PLAIN_MESSAGE));
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));list.setBackground(Theme.PAPER);p.add(new JScrollPane(list),BorderLayout.CENTER);return p;}
    private JComponent mapArea(){JPanel p=new JPanel(new BorderLayout(0,10));p.setBackground(Theme.PAPER);p.setBorder(Theme.pad(14,12,14,18));
        JPanel bar=new JPanel(new BorderLayout());bar.setBackground(Theme.CREAM);bar.setBorder(Theme.pad(10,12,10,12));bar.add(routeLabel);JButton clear=Theme.button("Clear route");clear.addActionListener(e->{map.setRouteVisible(false);routeLabel.setText("Select a washroom to explore");});bar.add(clear,BorderLayout.EAST);p.add(bar,BorderLayout.NORTH);p.add(map);return p;}
    public void renderList(List<WashroomListViewModel.Item> items){list.removeAll();for(var item:items){JPanel card=new JPanel(new BorderLayout(4,4));card.setMaximumSize(new Dimension(Integer.MAX_VALUE,106));card.setBackground(item.id().equals(selectedId)?Theme.PALE_BLUE:Theme.PAPER);
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(item.id().equals(selectedId)?Theme.BLUE:Theme.LINE),Theme.pad(10,10,10,10)));
            JLabel name=Theme.label(item.name(),14,item.id().equals(selectedId)?Theme.BLUE:Theme.INK);name.setFont(name.getFont().deriveFont(Font.BOLD));card.add(name,BorderLayout.NORTH);
            card.add(Theme.label(String.format("★ %.1f · %d m away",item.rating(),item.distanceMeters()),12,Theme.MUTED),BorderLayout.CENTER);
            JPanel actions=new JPanel(new GridLayout(1,2,6,0));actions.setOpaque(false);JButton reviews=Theme.button("Reviews"),directions=Theme.button("Directions");
            reviews.addActionListener(e->{selectedId=item.id();onReviews.accept(item.id());});directions.addActionListener(e->{selectedId=item.id();onDirections.accept(item.id());});actions.add(reviews);actions.add(directions);card.add(actions,BorderLayout.SOUTH);list.add(card);list.add(Box.createVerticalStrut(10));}list.revalidate();list.repaint();}
    public void setOnReviews(Consumer<String> c){onReviews=c;}public void setOnDirections(Consumer<String> c){onDirections=c;}
    public void setOnLogin(Runnable r){onLogin=r;}public void setOnRecommend(Runnable r){onRecommend=r;}public void setOnAccount(Runnable r){onAccount=r;}public void setOnReport(Runnable r){onReport=r;}public void setOnBusyness(Runnable r){onBusyness=r;}
    public double latitude(){return latitude;}public double longitude(){return longitude;}public String selectedId(){return selectedId;}
    private static final class CampusMapPanel extends JPanel{private boolean route;CampusMapPanel(){setBackground(Theme.CREAM);setBorder(BorderFactory.createLineBorder(Theme.LINE));}
        void setRouteVisible(boolean visible){route=visible;repaint();}@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            x.setColor(Theme.LINE);x.setStroke(new BasicStroke(8));int w=getWidth(),h=getHeight();x.drawLine(0,h/3,w,h/3-20);x.drawLine(0,2*h/3,w,2*h/3+15);x.drawLine(w/3,0,w/3-15,h);x.drawLine(2*w/3,0,2*w/3+15,h);
            if(route){x.setColor(new Color(55,138,221));x.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{2,9},0));x.drawPolyline(new int[]{w/5,w/3,w/3,2*w/3,3*w/4},new int[]{4*h/5,3*h/4,h/2,h/2,h/3},5);}
            x.setColor(Theme.BLUE);x.fillOval(w/5-7,4*h/5-7,14,14);x.setColor(Theme.BERRY);x.fillOval(3*w/4-8,h/3-8,16,16);x.dispose();}}
}
