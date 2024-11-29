package br.com.ControleFinanceiro.telas;

import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.TextAnchor;
import org.jfree.util.Rotation;

/**
 *
 * @author Anderson L
 */

public class MenCapTc5 extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public MenCapTc5() {
        initComponents();
        conexao = ModuloConexao.conector();

        if (conexao == null) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar ao banco de dados");
        } else {
            carregarAnos(); // Carregar anos
            
            // Listener para gerar gráficos ao selecionar ano no jComboBox1
            jComboBox1.addActionListener(evt -> {
                String anoSelecionado = (String) jComboBox1.getSelectedItem();
                if (anoSelecionado != null) {
                    criarGraficoComparativoMesesAno();
                    criarGraficoPizzaAno();
                }
            });
        }
    }


    private void carregarAnos() {
    String sql = "SELECT DISTINCT YEAR(data_pagamento) AS ano FROM transacoes " +
                 "WHERE data_pagamento IS NOT NULL ORDER BY ano";
    try (PreparedStatement pst = conexao.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {
        jComboBox1.removeAllItems();  // Limpa o JComboBox antes de adicionar novos anos
        while (rs.next()) {
            String ano = rs.getString("ano");
            jComboBox1.addItem(ano);  // Adiciona o ano ao JComboBox
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Erro ao carregar anos: " + e.getMessage());
    }
}
    


private void criarGraficoComparativoMesesAno() {
    String anoSelecionadoStr = (String) jComboBox1.getSelectedItem();
    if (anoSelecionadoStr == null) return;
    int anoSelecionado = Integer.parseInt(anoSelecionadoStr);
    int anoAnterior = anoSelecionado - 1;

    String sql = "SELECT MONTH(t.data_pagamento) AS mes, YEAR(t.data_pagamento) AS ano, " +
                 "tp.nome AS tipo, SUM(t.valor) AS total " +
                 "FROM transacoes t " +
                 "JOIN descricoes d ON t.descricao_id = d.id " +
                 "JOIN tipos tp ON d.tipo_id = tp.id " +
                 "WHERE (YEAR(t.data_pagamento) = ? OR YEAR(t.data_pagamento) = ?) " +
                 "GROUP BY YEAR(t.data_pagamento), MONTH(t.data_pagamento), tp.nome " +
                 "ORDER BY ano, mes";

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    try (PreparedStatement pst = conexao.prepareStatement(sql)) {
        pst.setInt(1, anoSelecionado);
        pst.setInt(2, anoAnterior);

        try (ResultSet rs = pst.executeQuery()) {
            double[][] receitas = new double[2][12];
            double[][] despesas = new double[2][12];

            while (rs.next()) {
                int mes = rs.getInt("mes") - 1;
                int ano = rs.getInt("ano");
                String tipo = rs.getString("tipo");
                double total = rs.getDouble("total");

                int anoIndex = (ano == anoSelecionado) ? 0 : 1;

                if (tipo.equalsIgnoreCase("Receita") && total != 0) {
                    receitas[anoIndex][mes] = total;
                } else if (tipo.equalsIgnoreCase("Despesa") && total != 0) {
                    despesas[anoIndex][mes] = total;
                }
            }

            for (int mes = 0; mes < 12; mes++) {
                String mesNome = Month.of(mes + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                
                if (receitas[0][mes] != 0) {
                    dataset.addValue(receitas[0][mes], "Receita " + anoSelecionado, mesNome);
                }
                if (despesas[0][mes] != 0) {
                    dataset.addValue(despesas[0][mes], "Despesa " + anoSelecionado, mesNome);
                }
                if (receitas[1][mes] != 0) {
                    dataset.addValue(receitas[1][mes], "Receita " + anoAnterior, mesNome);
                }
                if (despesas[1][mes] != 0) {
                    dataset.addValue(despesas[1][mes], "Despesa " + anoAnterior, mesNome);
                }
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Comparação de Receitas e Despesas - " + anoSelecionado + " vs " + anoAnterior,
                    "Mês",
                    "Valor (R$)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            barChart.setBackgroundPaint(new Color(245, 245, 245));
            barChart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
            barChart.getTitle().setPaint(new Color(34, 34, 34));

            CategoryPlot plot = (CategoryPlot) barChart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(new Color(200, 200, 200));
            plot.setRangeGridlinePaint(new Color(200, 200, 200));
            plot.setOutlineVisible(false);

            plot.getDomainAxis().setTickLabelPaint(Color.BLACK);
            plot.getRangeAxis().setTickLabelPaint(Color.BLACK);
            plot.getDomainAxis().setLabelPaint(Color.BLACK);
            plot.getRangeAxis().setLabelPaint(Color.BLACK);
            plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
            plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 12));
            plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

            BarRenderer3D renderer = new BarRenderer3D();
            renderer.setDrawBarOutline(false);
            renderer.setItemMargin(0.05);  // Reduz o espaço entre barras no mesmo mês
            renderer.setMaximumBarWidth(0.15); // Ajusta a largura das barras para melhor visualização

            // Aplicando as cores solicitadas com transparência
            renderer.setSeriesPaint(0, new Color(102, 255, 102, 180)); // Receita ano atual - Verde claro
            renderer.setSeriesPaint(1, new Color(255, 102, 102, 180)); // Despesa ano atual - Vermelho claro
            renderer.setSeriesPaint(2, new Color(102, 178, 255, 180)); // Receita ano anterior - Azul claro
            renderer.setSeriesPaint(3, new Color(0, 102, 204, 180));  // Despesa ano anterior - Azul mais escuro

            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getCurrencyInstance()));
            renderer.setBaseItemLabelsVisible(true);
            renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
            renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 10));
            renderer.setBaseItemLabelPaint(new Color(44, 44, 44));

            plot.setRenderer(renderer);

            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setPreferredSize(new Dimension(jPanel1.getWidth(), jPanel1.getHeight()));
            jPanel1.setLayout(new BorderLayout());
            jPanel1.removeAll();
            jPanel1.add(chartPanel, BorderLayout.CENTER);

            chartPanel.setMouseWheelEnabled(true);

            jPanel1.revalidate();
            jPanel1.repaint();
            pack();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    } catch (SQLException ex) {
        Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void criarGraficoPizzaAno() {
    String anoSelecionadoStr = (String) jComboBox1.getSelectedItem();
    if (anoSelecionadoStr == null) return;
    int anoSelecionado = Integer.parseInt(anoSelecionadoStr);

    // SQL atualizado para incluir diferentes tipos de transações
    String sql = "SELECT tp.nome AS tipo, SUM(t.valor) AS total " +
                 "FROM transacoes t " +
                 "JOIN descricoes d ON t.descricao_id = d.id " +
                 "JOIN tipos tp ON d.tipo_id = tp.id " +
                 "WHERE YEAR(t.data_pagamento) = ? " +
                 "GROUP BY tp.nome";

    DefaultPieDataset dataset = new DefaultPieDataset();

    try (PreparedStatement pst = conexao.prepareStatement(sql)) {
        pst.setInt(1, anoSelecionado);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String tipo = rs.getString("tipo");
                double total = rs.getDouble("total");

                // Adiciona valores ao dataset, excluindo valores zero
                if (total != 0) {
                    dataset.setValue(tipo, total);
                }
            }

            // Criando o gráfico de pizza 3D
            JFreeChart pieChart = ChartFactory.createPieChart3D(
                    "Distribuição de Receitas e Despesas - " + anoSelecionado,
                    dataset,
                    true, // Inclui legenda
                    true, // Inclui tooltips
                    false // Não exibe URLs
            );

            pieChart.setBackgroundPaint(new Color(245, 245, 245)); // Cor de fundo do gráfico
            pieChart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18)); // Título do gráfico
            pieChart.getTitle().setPaint(new Color(34, 34, 34));

            PiePlot3D plot = (PiePlot3D) pieChart.getPlot();
            plot.setBackgroundPaint(Color.WHITE); // Cor de fundo da área do gráfico
            plot.setOutlineVisible(false); // Desativa o contorno da pizza
            plot.setStartAngle(290); // Ângulo inicial da pizza
            plot.setDirection(Rotation.CLOCKWISE); // Direção das fatias
            plot.setForegroundAlpha(0.7f); // Opacidade das fatias

            // Configuração das cores para as fatias de receita e despesa
            plot.setSectionPaint("Receita", new Color(102, 255, 102, 180));  // Verde claro para receita
            plot.setSectionPaint("Despesa", new Color(255, 102, 102, 180));  // Vermelho claro para despesa

            // Adicionando cores para outras categorias, se houver
            plot.setSectionPaint("Investimento", new Color(255, 215, 0, 180));  // Amarelo para investimentos
            plot.setSectionPaint("Outros", new Color(153, 153, 255, 180));  // Azul claro para outros

            // Configuração dos rótulos da fatia
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                    "{0}: {1} ({2})", NumberFormat.getCurrencyInstance(), NumberFormat.getPercentInstance()
            ));
            plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12)); // Fonte do rótulo
            plot.setLabelPaint(new Color(44, 44, 44)); // Cor do rótulo
            plot.setLabelBackgroundPaint(new Color(220, 220, 220)); // Cor do fundo do rótulo
            plot.setLabelOutlinePaint(null); // Sem contorno no rótulo
            plot.setLabelShadowPaint(null); // Sem sombra no rótulo

            // Painel para adicionar o gráfico
            ChartPanel chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new Dimension(jPanel2.getWidth(), jPanel2.getHeight()));
            jPanel2.setLayout(new BorderLayout());
            jPanel2.removeAll();
            jPanel2.add(chartPanel, BorderLayout.CENTER);

            // Permite interação com a roda do mouse (zoom)
            chartPanel.setMouseWheelEnabled(true);

            // Atualiza o painel e a janela
            jPanel2.revalidate();
            jPanel2.repaint();
            pack();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    } catch (SQLException ex) {
        Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
    }
}


private String obterNomeMes(int mes) {
    String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                      "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
    if (mes >= 1 && mes <= 12) {
        return meses[mes - 1];
    }
    return "Mês inválido";
}



private String mesNome(int mes) {
    return new DateFormatSymbols().getMonths()[mes - 1];
}

private Color gerarCorPorMes(int mes) {
    return Color.getHSBColor((float) mes / 12, 0.8f, 0.9f); // Cores suaves e diversificadas para cada mês
}


    // Método para fechar recursos
    private void fecharRecursos() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPaneCustom1 = new raven.tabbed.TabbedPaneCustom();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Graficos Anual");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1144, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 623, Short.MAX_VALUE)
        );

        tabbedPaneCustom1.addTab("Entrada e Saída", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1144, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 623, Short.MAX_VALUE)
        );

        tabbedPaneCustom1.addTab("Entrada e Saída Anual", jPanel2);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Ano");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPaneCustom1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(tabbedPaneCustom1, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                .addContainerGap())
        );

        setBounds(0, 0, 1185, 775);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private raven.tabbed.TabbedPaneCustom tabbedPaneCustom1;
    // End of variables declaration//GEN-END:variables
}
