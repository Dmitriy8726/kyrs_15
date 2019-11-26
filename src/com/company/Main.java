package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

class VerticalLayout implements LayoutManager
{
    private Dimension size = new Dimension();

    // Следующие два метода не используются
    public void addLayoutComponent   (String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

    // Метод определения минимального размера для контейнера
    public Dimension minimumLayoutSize(Container c) {
        return calculateBestSize(c);
    }
    // Метод определения предпочтительного размера для контейнера
    public Dimension preferredLayoutSize(Container c) {
        return calculateBestSize(c);
    }
    // Метод расположения компонентов в контейнере
    public void layoutContainer(Container container)
    {
        // Список компонентов
        Component list[] = container.getComponents();
        int currentY = 5;
        for (int i = 0; i < list.length; i++) {
            // Определение предпочтительного размера компонента
            Dimension pref = list[i].getPreferredSize();
            // Размещение компонента на экране
            list[i].setBounds(5, currentY, pref.width, pref.height);
            // Учитываем промежуток в 5 пикселов
            currentY += 5;
            // Смещаем вертикальную позицию компонента
            currentY += pref.height;
        }
    }
    // Метод вычисления оптимального размера контейнера
    private Dimension calculateBestSize(Container c)
    {
        // Вычисление длины контейнера
        Component[] list = c.getComponents();
        int maxWidth = 0;
        for (int i = 0; i < list.length; i++) {
            int width = list[i].getWidth();
            // Поиск компонента с максимальной длиной
            if ( width > maxWidth )
                maxWidth = width;
        }
        // Размер контейнера в длину с учетом левого отступа
        size.width = maxWidth + 5;
        // Вычисление высоты контейнера
        int height = 0;
        for (int i = 0; i < list.length; i++) {
            height += 5;
            height += list[i].getHeight();
        }
        size.height = height;
        return size;
    }
}

public class Main {

    private static String jt_str_begin = "S", jt_str_numb, jt_str_nume;
    private static int num_begin, num_end;
    private static String[] simbol = {"S", "A", "B", "C", "N", "D", "E", "F", "G", "H", "J"};
    private static ArrayList<String> str_temp_term = new ArrayList<>();
    private static ArrayList<String> str_temp_noterm = new ArrayList<>();
    private static ArrayList<String> str_Temp_noterm_True = new ArrayList<>(); // верный список нетерминалов
    private static ArrayList<String> str_Temp_noterm_DKA = new ArrayList<>(); // DKA нетерминалов
    private static HashMap<String, ArrayList<String>> rule = new HashMap<>();
    private static HashMap<String, ArrayList<String>> rule_True = new HashMap<>(); // верный список правил
    private static HashMap<String, HashMap<String, String>> NKA_map = new HashMap<>(); //НКА
    private static HashMap<String, HashMap<String, String>> DKA_map = new HashMap<>(); //ДКА
    private static HashMap<String, HashMap<String, String>> DKA_map_True = new HashMap<>(); //ДКА без лишних состояний
    private static ArrayList<String> end_pos = new ArrayList<>(); //Множество конечных состояний
    private static HashMap<Integer, HashMap<String, String>> number_chain = new HashMap<>();
    private static HashMap<String, ArrayList<String>> answer = new HashMap<>();
    private static JFrame myWindow;
    private static JPanel jp_main;
    private static JPanel jp_rule;
    private static JPanel jp_table;
    private static JPanel jp_chain;
    private static int index = 0;

    public static void main(String[] args) {
        myWindow = new JFrame("KC");
        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menubar = new JMenuBar();
        // создаем меню
        menubar.add(info(myWindow));
        jp_main = new JPanel(new VerticalLayout());
        jp_rule = new JPanel(new VerticalLayout());
        jp_table = new JPanel();
        jp_chain = new JPanel(new VerticalLayout());
        //Панель для ввода
        JPanel panel1 = new JPanel();
        JLabel lb_str = new JLabel("Регулярное выражение: ");
        JTextField jt_str = new JTextField(15);
        panel1.add(lb_str);
        panel1.add(jt_str);
        jp_main.add(panel1);
        //Диапазон
        panel1 = new JPanel();
        JLabel jl_diap_beg = new JLabel("От: ");
        JTextField jt_diap_beg = new JTextField(2);
        panel1.add(jl_diap_beg);
        panel1.add(jt_diap_beg);
        JLabel jl_diap_end = new JLabel("До: ");
        JTextField jt_diap_end = new JTextField(2);
        panel1.add(jl_diap_end);
        panel1.add(jt_diap_end);
        jp_main.add(panel1);
        JButton check = new JButton("Next");
        check.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                jt_str_numb = jt_diap_beg.getText();
                jt_str_nume = jt_diap_end.getText();
                String regex = "-?\\d+(\\.\\d+)?";
                if (!(jt_str_numb.matches(regex)) || !(jt_str_nume.matches(regex))) {
                    JOptionPane.showMessageDialog(myWindow, "ОШИБКА, ДИАПАЗОН ЗАДАЕТСЯ ТОЛЬКО ЧИСЛАМИ");
                    return;
                } else {
                    num_begin = Integer.parseInt(jt_str_numb);
                    num_end = Integer.parseInt(jt_str_nume);
                    if (num_begin == 0 || num_end == 0 || num_end - num_begin <= 0) {
                        JOptionPane.showMessageDialog(myWindow, "ОШИБКА, ДИАПАЗОН ЗАДАН НЕ ВЕРНО");
                        return;
                    }
                }

                if (jt_str.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(myWindow, "ОШИБКА, ПУСТОЕ ПОЛЕ");
                    return;
                }


                jp_main.setVisible(false);
                str_temp_term.add("#");
                if (regul_Gram(jt_str.getText())) {
                    jp_main.setVisible(false);
                    str_Temp_noterm_True.addAll(str_temp_noterm);
                    NKA();
                    DKA();
                }

            }
        });
        myWindow.add(jp_main);
        jp_main.add(check);
        // добавляем панель меню в окно
        myWindow.setJMenuBar(menubar);
        myWindow.setVisible(true);
        myWindow.setSize(800, 300);
        myWindow.setResizable(false);

    }

    public static JMenu info(JFrame glav) {
        JMenu file = new JMenu("Инфо");
        JMenuItem avtor = new JMenuItem(new AbstractAction("Автор") {
            public void actionPerformed(ActionEvent e) {
                glav.setEnabled(false);
                JFrame myWindow = new JFrame("Автор");
                myWindow.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        glav.setEnabled(true);
                    }
                });
                JPanel panel = new JPanel();
                JLabel lab = new JLabel("<html><p align=\"center\">Студент 4 курса . группы ИП-613<br />Плотников А.В.</p></html>");
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalGlue());
                panel.add(lab);
                panel.add(Box.createHorizontalGlue());
                myWindow.setContentPane(panel);
                myWindow.setVisible(true);
                myWindow.setSize(220, 100);
                myWindow.setResizable(false);
            }
        });
        JMenuItem tema = new JMenuItem(new AbstractAction("Тема") {
            public void actionPerformed(ActionEvent e) {
                glav.setEnabled(false);
                JFrame myWindow = new JFrame("Тема");
                myWindow.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        glav.setEnabled(true);
                    }
                });
                JPanel panel = new JPanel();

                JLabel lab = new JLabel("<html><p align=\"justify \">Написать программу, которая по заданной регулярной грамматике" +
                        "<br />(грамматика может быть НЕ автоматного вида!, ЛЛ или ПЛ) построит " +
                        "<br />эквивалентный ДКА (представление функции переходов в виде табли-" +
                        "<br />цы). Программа должна сгенерировать по исходной грамматике не-" +
                        "<br />сколько цепочек в заданном диапазоне длин и проверить их допусти-" +
                        "<br />мость построенным автоматом. Процессы построения цепочек и провер- " +
                        "<br />ки их выводимости отображать на экране (по требованию). Предусмот-" +
                        "<br />реть возможность проверки цепочки, введённой пользователем. " +
                        "</p></html>");
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalGlue());
                panel.add(lab);
                panel.add(Box.createHorizontalGlue());
                myWindow.setContentPane(panel);
                myWindow.setVisible(true);
                myWindow.setSize(465, 300);
                myWindow.setResizable(false);
            }
        });
        JMenuItem notet = new JMenuItem(new AbstractAction("Правила") {
            public void actionPerformed(ActionEvent e) {
                glav.setEnabled(false);
                JFrame myWindow = new JFrame("Правила");
                myWindow.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        glav.setEnabled(true);
                    }
                });
                JPanel panel = new JPanel();
                JLabel lab = new JLabel("<html><p>Ввод производить через пробел, запрещенные символы:скобки, символы не принадлежащие латинице, не больше 1 символа<br /></p></html>");
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalGlue());
                panel.add(lab);
                panel.add(Box.createHorizontalGlue());
                myWindow.setContentPane(panel);
                myWindow.setVisible(true);
                myWindow.setSize(220, 100);
                myWindow.setResizable(false);
            }
        });
        file.add(avtor);
        file.add(notet);
        file.add(tema);
        return file;
    }

    public static boolean regul_Gram(String pp) {
        if (pp.equals("")) {
            JOptionPane.showMessageDialog(myWindow, "ПУСТАЯ СТРОКА");
            return false;
        }

        ArrayList<String> str = new ArrayList<>();
        ArrayList <String> str_temp = new ArrayList<>();
        boolean flag_term = true;
        int rex = 0;

        for (int i = 1; i < pp.length(); i++) {
            if (pp.substring(i, i + 1).equals("*")) {
                rex = i + 1;
                break;
            }
            if (pp.substring(i, i + 1).equals("(")) {
                if (str.isEmpty()) {
                    for (int j = i + 1; j < pp.length(); j++) {
                        if (!pp.substring(j, j + 1).equals(")")) {
                            if (!pp.substring(j, j + 1).equals("+")) {
                                str.add(pp.substring(j, j + 1));
                                for (String s: str_temp_term) {
                                    if (s.equals(pp.substring(j, j + 1))) {
                                        flag_term = false;
                                    }
                                }
                                if (flag_term) {
                                    str_temp_term.add(pp.substring(j, j + 1));
                                }
                                flag_term = true;

                            }
                        } else {
                            i = j;
                            break;
                        }

                    }
                } else {
                    for (int j = i + 1; j < pp.length(); j++) {
                        if (!pp.substring(j, j + 1).equals(")")) {
                            if (!pp.substring(j, j + 1).equals("+")) {
                                for (String s: str_temp_term) {
                                    if (s.equals(pp.substring(j, j + 1))) {
                                        flag_term = false;
                                    }
                                }
                                if (flag_term) {
                                    str_temp_term.add(pp.substring(j, j + 1));
                                }
                                flag_term = true;
                                for (String s: str) {
                                    str_temp.add(s + pp.substring(j, j + 1));
                                }
                            }

                        } else {
                            str.clear();
                            str.addAll(str_temp);
                            str_temp = new ArrayList<>();
                            i = j;
                            break;
                        }
                    }
                }
            } else {
                if (!pp.substring(i, i + 1).equals("+") && !pp.substring(i, i + 1).equals(")")) {
                    for (String s: str_temp_term) {
                        if (s.equals(pp.substring(i, i + 1))) {
                            flag_term = false;
                        }
                    }
                    if (flag_term) {
                        str_temp_term.add(pp.substring(i, i + 1));
                    }
                    flag_term = true;
                    str.add(pp.substring(i, i + 1));

                }
            }
        }



        for (String s: str) {
            str_temp.add(s + "S");
        }
        for (String s: str_temp_term) {
            if (s.equals(pp.substring(rex, rex + 1))) {
                flag_term = false;
            }
        }
        if (flag_term) {
            str_temp_term.add(pp.substring(rex, rex + 1));
        }

        str_temp.add(pp.substring(rex, rex + 1) + "A");
        System.out.println(str_temp);

        str_temp_noterm.add("S");
        str_temp_noterm.add("A");

        String ttp = "A";
        String ccc = "";
        rule.put("S", str_temp);
        str_temp = new ArrayList<>();
        boolean flag = true;
        for (int i = rex + 1; i < pp.length(); i++) {
            if (i == pp.length() - 1) {
                str_temp.add(pp.substring(i, i + 1));
                rule.put(ttp, str_temp);
                break;
            }
            for (String s: simbol) {
                for (String ss:str_temp_noterm) {
                    if (s.equals(ss)) {
                        flag = false;
                    }
                }
                if (flag) {
                    ccc = s;
                    str_temp_noterm.add(ccc);
                    break;
                }
                flag =true;
            }
            flag = true;
            str_temp.add(pp.substring(i, i + 1) + ccc);
            rule.put(ttp, str_temp);
            str_temp = new ArrayList<>();
            ttp = ccc;
        }
        System.out.println(str_temp_noterm);
        System.out.println(str_temp_term);
        chain_Avto();
        return true;

    }


   public static boolean chain_Avto() {
        int flag = 0;
        for (String s: str_temp_noterm) {
            for (String str : rule.get(s)) {
                if(str.length() == 2) {
                    if (check_Simbol(str.substring(0, 1), str_temp_term) && check_Simbol(str.substring(1, 2), str_temp_noterm)) {
                        flag = 1;
                        break;
                    } else if (check_Simbol(str.substring(0, 1), str_temp_noterm) && check_Simbol(str.substring(1, 2), str_temp_term)) {
                        flag = 2;
                        break;
                    }
                }
            }
            if (flag != 0) {
                break;
            }
        }

        ArrayList <String> temp = new ArrayList<>();
        if (flag == 0 || flag == 1) {
            for (String s: str_temp_noterm) {
                for (String str : rule.get(s)) {
                    if (str.length() > 2) {
                        if (str.length() - countChar(str) == 1 && check_Simbol(str.substring(str.length() - 1), str_temp_noterm) ) {
                            String s_temp_nt = find_Simbol();
                            String s_temp_nt1 = "";
                            temp.add(str.substring(0 , 1) + s_temp_nt);
                            ArrayList <String> ss = new ArrayList<>();
                            for (int i = 1; i < countChar(str) - 1; i++ ) {
                                s_temp_nt1 = find_Simbol();
                                ss.add(str.substring(i, i + 1) + s_temp_nt1);
                                rule_True.put(s_temp_nt, ss);
                                s_temp_nt = s_temp_nt1;
                                ss = new ArrayList<>();
                            }
                            ss.add(str.substring(str.length() - 2));
                            rule_True.put(s_temp_nt, ss);

                        } else if (str.length() - countChar(str) == 0) {
                            String s_temp_nt = find_Simbol();
                            String s_temp_nt1 = "";
                            temp.add(str.substring(0 , 1) + s_temp_nt);
                            ArrayList <String> ss = new ArrayList<>();
                            for (int i = 1; i < countChar(str) - 1; i++ ) {
                                s_temp_nt1 = find_Simbol();
                                ss.add(str.substring(i, i + 1) + s_temp_nt1);
                                rule_True.put(s_temp_nt, ss);
                                s_temp_nt = s_temp_nt1;
                                ss = new ArrayList<>();
                            }
                            ss.add(str.substring(str.length() - 1) + "T");
                            rule_True.put(s_temp_nt, ss);
                        } else {
                            return false;
                        }
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_term) && check_Simbol(str.substring(1, 2), str_temp_term)) {
                        String s_temp_nt = find_Simbol();
                        String s_temp_nt1 = "";
                        temp.add(str.substring(0 , 1) + s_temp_nt);
                        ArrayList <String> ss = new ArrayList<>();
                        for (int i = 1; i < countChar(str) - 1; i++ ) {
                            s_temp_nt1 = find_Simbol();
                            ss.add(str.substring(i, i + 1) + s_temp_nt1);
                            rule_True.put(s_temp_nt, ss);
                            s_temp_nt = s_temp_nt1;
                            ss = new ArrayList<>();
                        }
                        ss.add(str.substring(str.length() - 1) + "T");
                        rule_True.put(s_temp_nt, ss);
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_noterm) && check_Simbol(str.substring(1, 2), str_temp_noterm)) {
                        return false;
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_noterm) && check_Simbol(str.substring(1, 2), str_temp_term)) {
                        return false;
                    } else if (str.length() == 1 && check_Simbol(str.substring(0, 1), str_temp_noterm)) {
                        return false;
                    } else {
                        temp.add(str);
                    }
                }
                rule_True.put(s, temp);
                temp = new ArrayList<>();
            }

        } else {
            for (String s: str_temp_noterm) {
                for (String str : rule.get(s)) {
                    if (str.length() > 2) {
                        if (str.length() - countChar(str) == 0) {
                            String s_temp_nt = find_Simbol();
                            String s_temp_nt1 = "";
                            temp.add(s_temp_nt + str.substring(str.length() - 1));
                            ArrayList <String> ss = new ArrayList<>();
                            for (int i = str.length() - 2; i > 0; i-- ) {
                                s_temp_nt1 = find_Simbol();
                                ss.add(s_temp_nt1 + str.substring(i, i + 1));
                                rule_True.put(s_temp_nt, ss);
                                s_temp_nt = s_temp_nt1;
                                ss = new ArrayList<>();
                            }
                            ss.add("T" + str.substring(0, 1));
                            rule_True.put(s_temp_nt, ss);
                        } else {
                            return false;
                        }
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_term) && check_Simbol(str.substring(1, 2), str_temp_term)) {
                        String s_temp_nt = find_Simbol();
                        String s_temp_nt1 = "";
                        temp.add(s_temp_nt + str.substring(str.length() - 1));
                        ArrayList <String> ss = new ArrayList<>();
                        for (int i = str.length() - 2; i > 0; i-- ) {
                            s_temp_nt1 = find_Simbol();
                            ss.add(s_temp_nt1 + str.substring(i, i + 1));
                            rule_True.put(s_temp_nt, ss);
                            s_temp_nt = s_temp_nt1;
                            ss = new ArrayList<>();
                        }
                        ss.add("T" + str.substring(0, 1));
                        rule_True.put(s_temp_nt, ss);
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_noterm) && check_Simbol(str.substring(1, 2), str_temp_noterm)) {
                        return false;
                    } else if (str.length() == 2 && check_Simbol(str.substring(0, 1), str_temp_term) && check_Simbol(str.substring(1, 2), str_temp_noterm)) {
                        return false;
                    } else if (str.length() == 1 && check_Simbol(str.substring(0, 1), str_temp_noterm)) {
                        return false;
                    } else {
                        temp.add(str);
                    }
                }
                rule_True.put(s, temp);
                temp = new ArrayList<>();
            }
        }
        temp = new ArrayList<>();
        temp.add("#");
        rule_True.put("T", temp);
        return true;

    }

    public static boolean check_Simbol(String s, ArrayList<String> list) {
        for (String str: list) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static int countChar(String str)
    {
        int count = 0;
        for (String c: str_temp_term) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == c.charAt(0))
                    count++;
            }
        }

        return count;
    }
    public static String find_Simbol() {
        boolean flag_nt = true;
        for (String str1: simbol) {
            for (String str2: str_Temp_noterm_True) {
                if (str1.equals(str2)) {
                    flag_nt = false;
                }
            }
            if (flag_nt) {
                str_Temp_noterm_True.add(str1);
                return str1;
            }
            flag_nt = true;
        }
        return null;
    }

    public static void NKA() {
        end_pos.add("T");
        str_temp_noterm.add("T");
        str_Temp_noterm_True.add("T");
        HashMap <String, String> temp = new HashMap<>();
        for (String str3: str_temp_term) {
            for (String str1: str_Temp_noterm_True) {
                String p = "";
                for (String str2: rule_True.get(str1)) {
                    if (str2.contains(str3)) {
                        if (str2.length() == 2) {
                            if (check_Simbol(str2.substring(0, 1), str_Temp_noterm_True)) {

                                p += str2.substring(0, 1);
                            } else {
                                p += str2.substring(1, 2);
                            }
                        } else {
                            p += str1;
                            end_pos.add(str1);
                        }
                    }
                }
                boolean flag_dka = true;
                for (String t_s:str_Temp_noterm_DKA) {
                    if (p.equals(t_s) && !p.isEmpty()) {
                        flag_dka = false;
                        break;
                    }
                }
                if (flag_dka && !p.isEmpty()) {
                    boolean flag_end = true;
                    for (String pts: end_pos) {
                        if (p.equals(pts)) {
                            flag_end = false;
                        }
                    }
                    if (flag_end) {
                        boolean flag_end1 = false;
                        for (String pts: end_pos) {
                            if (p.contains(pts)) {
                                flag_end1 = true;
                            }
                        }
                        if (flag_end1) {
                            end_pos.add(p);
                        }
                    }
                    str_Temp_noterm_DKA.add(p);
                }
                temp.put(str1, p);
            }
            if (!str3.equals("#")) {
                NKA_map.put(str3, temp);
            }
            temp = new HashMap<>();

        }
    }

    public static void DKA() {
        HashMap<String, String> temp = new HashMap<>();
        ArrayList<String> m1 = new ArrayList<>(str_Temp_noterm_DKA);
        while (true) {
            for (String str1 : str_temp_term) {
                if (str1.equals("#")) {
                    continue;
                }
                for (String str2 : str_Temp_noterm_DKA) {
                    String p = "";
                    boolean flag_dka = true;
                    for (String t_s : str_Temp_noterm_True) {
                        if (str2.equals(t_s)) {
                            flag_dka = false;
                            break;
                        }
                    }
                    if (flag_dka) {
                        String pp = "";
                        for (int i = 0; i < str2.length(); i++) {
                            pp += NKA_map.get(str1).get(str2.substring(i, i + 1));
                        }

                        temp.put(str2, pp);
                        boolean flag_dka1 = true;
                        for (String t_s : str_Temp_noterm_DKA) {
                            if (pp.equals(t_s)) {
                                flag_dka1 = false;
                                break;
                            }
                        }
                        if (flag_dka1 && !pp.isEmpty()) {
                            boolean flag_end = true;
                            for (String pts: end_pos) {
                                if (pp.equals(pts)) {
                                    flag_end = false;
                                }
                            }
                            if (flag_end) {
                                boolean flag_end1 = false;
                                for (String pts: end_pos) {
                                    if (pp.contains(pts)) {
                                        flag_end1 = true;
                                    }
                                }
                                if (flag_end1) {
                                    end_pos.add(pp);
                                }
                            }
                            m1.add(pp);
                        }
                    } else {

                        temp.put(str2, NKA_map.get(str1).get(str2));
                    }
                }
                DKA_map.put(str1, temp);
                temp = new HashMap<>();
            }
            if (m1.size() == str_Temp_noterm_DKA.size()) {
                break;
            } else {
                DKA_map.clear();
                str_Temp_noterm_DKA.clear();
                str_Temp_noterm_DKA.addAll(m1);
            }
        }


        ArrayList <String> true_nonterm;
        ArrayList <String> temp_nonterm = new ArrayList<>();
        temp_nonterm.add(jt_str_begin);
        System.out.println(DKA_map);
        do {
            true_nonterm = new ArrayList<>();
            true_nonterm.addAll(temp_nonterm);
            for (String fnt: true_nonterm) {
                for (String ft: str_temp_term) {
                    if (ft.equals("#")) {
                        continue;
                    }
                    String lom = DKA_map.get(ft).get(fnt);
                    if(!lom.isEmpty()) {
                        boolean flag_end = true;
                        for (String pts: true_nonterm) {
                            if (lom.equals(pts)) {
                                flag_end = false;
                            }
                        }
                        if (flag_end) {
                            temp_nonterm.add(lom);
                        }
                    }
                }
            }
        }while (true_nonterm.size() != temp_nonterm.size());

        System.out.println(true_nonterm);

        for (String str1: str_temp_term) {
            if (str1.equals("#")) {
                continue;
            }
            HashMap <String, String> pp = new HashMap<>();
            for (String str2: true_nonterm) {
                pp.put(str2, DKA_map.get(str1).get(str2));
            }
            DKA_map_True.put(str1, pp);
        }

        System.out.println(DKA_map_True);

        String[] stp = new String[str_temp_term.size()];
        int i = 1;
        stp[0] = "";
        for (String str: str_temp_term) {
            if (str.equals("#")) {
                continue;
            }
            stp[i] = str;
            i++;
        }

        String[][] stp_mas = new String[true_nonterm.size()][str_temp_term.size()];
        for (int j = 0; j < true_nonterm.size(); j++) {
            String[] st = new String[str_temp_term.size()];
            st[0] = true_nonterm.get(j);
            int f = 1;
            for (String spo: str_temp_term) {
                if (spo.equals("#")) {
                    continue;
                }

                st[f] = DKA_map_True.get(spo).get(true_nonterm.get(j));
                f++;
            }
            stp_mas[j] = st;
        }

        JTable table = new JTable(stp_mas,stp);
        JScrollPane scroll = new JScrollPane(table);
        jp_table.add(scroll);
        JButton ne = new JButton("Next");
        ne.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generation(rule_True, str_Temp_noterm_True, answer, number_chain);
                jp_table.setVisible(false);
                System.out.println(answer);
                System.out.println(number_chain);
                JTextArea textArea = new JTextArea(7, 60);
                for (int i = num_begin; i <= num_end; i++) {
                    String pt = new String();
                    for(String str: answer.get(Integer.toString(i))) {
                        String sss = number_chain.get(i).get(str);
                        for (int j = 1; j <= sss.length(); j++) {
                            for (String p: number_chain.get(i).keySet()) {
                                if (number_chain.get(i).get(p).equals(sss.substring(0, j)))
                                {
                                    pt += p + " -> ";
                                    break;
                                }
                            }
                        }
                        textArea.append( pt + "ЦЕПОЧКА ПОЛУЧЕНА \n");
                        textArea.append(DKA_check(str));
                        pt = new String();
                    }
                }

                JScrollPane scroll1 = new JScrollPane(textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jp_chain.add(scroll1);
                JPanel panel = new JPanel();
                JButton button = new JButton("Check");
                JTextField text = new JTextField(15);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JOptionPane.showMessageDialog(myWindow, DKA_check(text.getText()));
                    }
                });

                panel.add(text);
                panel.add(button);
                jp_chain.add(panel);
                myWindow.add(jp_chain);


            }
        });
        jp_table.add(ne);
        myWindow.add(jp_table);
    }

    public static void generation(HashMap <String, ArrayList<String>> map,  ArrayList <String> list,
                                  HashMap <String, ArrayList<String>> ans, HashMap<Integer, HashMap<String, String>> chain) {
        boolean flag_all = true;
        boolean flag_term = true;
        boolean flag_add = true;
        int counter = 0;
        int add_tree = 1;
        ArrayList<String> m1 = new ArrayList<>();
        ArrayList<String> m2 = new ArrayList<>();
        ArrayList<String> answer = new ArrayList<>();
        String split1 = "", split2 = "";
        String s = null;
        String ss = null;
        HashMap <String, String> chain_tamp = new HashMap<>();
        m1.add(jt_str_begin);
        for (int f = num_begin; f <= num_end; f++) {
            answer = new ArrayList<>();
            flag_all = true;
            flag_term = true;
            flag_add = true;
            m1.clear();
            m2.clear();
            split1 = "";
            split2 = "";
            s = null;
            ss = null;
            m1.add(jt_str_begin);
            counter = 0;
            chain_tamp = new HashMap<>();
            chain_tamp.put("S", "1");
            while (flag_all) {
                for (String str1 : m1) {
                    split1 = "";
                    split2 = "";
                    counter = 0;
                    flag_term = true;
                    add_tree = 1;
                    while (flag_term) {
                        s = str1.substring(counter, counter + 1);
                        for (String str2 : list) {
                            if (str2.equals(s)) {
                                flag_term = false;
                                if (counter == 0) {
                                    split2 = str1.substring(counter + 1);
                                } else {
                                    split2 = str1.substring(counter + 1);
                                    split1 = str1.substring(0, counter);
                                }
                                counter = 0;
                            }
                        }

                        counter++;
                    }

                    for (String str : map.get(s)) {
                        flag_add = true;
                        ss = split1 + str + split2;
                        if (str.equals("#")) {
                            ss = split1 + split2;
                        }
                        if (countCharsimple(ss) > f) {
                            continue;
                        }


                        for (String str3 : list) {
                            if (ss.contains(str3)) {
                                flag_add = false;
                                m2.add(ss);
                                chain_tamp.put(ss, chain_tamp.get(str1) + add_tree);
                                add_tree++;
                                break;
                            }
                        }

                        boolean flag = true;
                        if (flag_add && ss.length() == f) {
                            chain_tamp.put(ss, chain_tamp.get(str1) + add_tree);
                            add_tree++;
                            for (String sss : answer) {
                                if (sss.equals(ss)) {
                                    flag = false;
                                }
                            }
                            if (flag) {
                                answer.add(ss);
                            }
                        }
                    }
                }

                m1.clear();
                for (String str : m2) {
                    m1.add(str);
                }
                m2.clear();
                if (m1.isEmpty()) {
                    flag_all = false;
                }
            }
            ans.put(Integer.toString(f), answer);
            chain.put(f, chain_tamp);
        }

    }

    public static int countCharsimple(String str)
    {
        int count = 0;
        for (String c: str_temp_term) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == c.charAt(0))
                    count++;
            }
        }

        return count;
    }

    public static String DKA_check(String str) {
        String sost = jt_str_begin;
        String s = "{ " + sost + " , " + str + " } |- ";
        try {

            for (int i = 0; i < str.length(); i++) {
                sost = DKA_map_True.get(str.substring(i, i + 1)).get(sost);
                s += "{ " + sost + " , " + str.substring(i + 1) + " } |- ";
                System.out.println(s);
            }
            boolean spt = true;
            for (String ks : end_pos) {
                if (ks.equals(sost)) {
                    spt = false;
                }
            }
            if (spt) {
                JOptionPane.showMessageDialog(myWindow, "ОШИБКА, КОНЕЧНОЕ СОСТОЯНИЕ НЕ ДОСТИГНУТО");
                return "ERROR";
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(myWindow, "ЦЕПОЧКА НЕВОЗМОЖНА");
            return "ERROR";
        }
        return s + "ЦЕПОЧА ВЕРНАЯ \n";
    }

}