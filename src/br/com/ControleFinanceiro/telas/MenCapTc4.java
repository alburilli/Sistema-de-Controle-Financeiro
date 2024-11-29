package br.com.ControleFinanceiro.telas;

import br.com.ControleFinanceiro.dal.ModuloConexao;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Anderson L
 */

public class MenCapTc4 extends javax.swing.JInternalFrame {
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public MenCapTc4() {
        initComponents();
        conexao = ModuloConexao.conector();
        popularComboAnos();



        // Eventos para carregar a tabela conforme a seleção do ano
        cmbAnos.addActionListener(evt -> atualizarTabelas());
    }

    private void atualizarTabelas() {
        String anoSelecionado = (String) cmbAnos.getSelectedItem();
        if (anoSelecionado != null) {
            popularTabelaEntradas(anoSelecionado);
            popularTabelaSaidas(anoSelecionado);
        }
    }



    private void popularComboAnos() {
        String sql = "SELECT DISTINCT YEAR(data_pagamento) AS ano FROM transacoes WHERE data_pagamento IS NOT NULL ORDER BY ano";
        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                model.addElement(rs.getString("ano"));
            }
            cmbAnos.setModel(model);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar os anos: " + e.getMessage());
        } finally {
            fecharRecursos();
        }
    }

   private void popularTabelaEntradas(String anoSelecionado) {
        DefaultTableModel model = (DefaultTableModel) tblEntradas.getModel();
        model.setRowCount(0);

        // Remover a lógica de status
        String sql = "SELECT d.descricao AS descricao, MONTH(t.data_pagamento) AS mes, SUM(t.valor) AS total "
                   + "FROM transacoes t "
                   + "JOIN descricoes d ON t.descricao_id = d.id "
                   + "JOIN tipos ty ON d.tipo_id = ty.id "
                   + "WHERE YEAR(t.data_pagamento) = ? AND ty.nome = 'Receita' "
                   + "GROUP BY d.descricao, mes "
                   + "ORDER BY d.descricao ASC, mes";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, anoSelecionado);
            rs = pst.executeQuery();
            Map<String, double[]> receitasMensais = new HashMap<>();
            double[] totalReceitasPorMes = new double[12];

            while (rs.next()) {
                String descricao = rs.getString("descricao");
                double valor = rs.getDouble("total");
                int mes = rs.getInt("mes") - 1;

                receitasMensais.computeIfAbsent(descricao, k -> new double[12])[mes] += valor;
                totalReceitasPorMes[mes] += valor;
            }

            for (Map.Entry<String, double[]> entry : receitasMensais.entrySet()) {
                adicionarLinhaTabelaComTotal(model, entry.getKey(), entry.getValue());
            }

            // Calcule o total anual e adicione à tabela
            double totalAnualReceitas = calcularTotalAnual(totalReceitasPorMes);
            adicionarLinhaTabelaTotalAnual(model, "Total de Receitas", totalReceitasPorMes, totalAnualReceitas);
            ajustarLarguraColunas(tblEntradas);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar as entradas: " + e.getMessage());
        } finally {
            fecharRecursos();
        }
    }

// Método auxiliar para adicionar linhas com totais mensais
private void adicionarLinhaTabelaComTotal(DefaultTableModel model, String descricao, double[] totaisMensais) {
    Object[] row = new Object[14]; // Assumindo 12 meses + 1 para total + 1 para algum outro valor se necessário
    row[0] = descricao; // Coluna de descrição
    for (int i = 0; i < 12; i++) {
        row[i + 1] = formatarValor(totaisMensais[i]); // Adiciona os totais mensais
    }
    row[13] = formatarValor(calcularTotalAnual(totaisMensais)); // Adiciona o total anual
    model.addRow(row); // Adiciona a linha ao modelo da tabela
}

   private void popularTabelaSaidas(String anoSelecionado) {
        DefaultTableModel model = (DefaultTableModel) tblSaidas.getModel();
        model.setRowCount(0);

        // Remover a lógica de status
        String sql = "SELECT d.descricao AS descricao, MONTH(t.data_pagamento) AS mes, SUM(t.valor) AS total "
                   + "FROM transacoes t "
                   + "JOIN descricoes d ON t.descricao_id = d.id "
                   + "JOIN tipos ty ON d.tipo_id = ty.id "
                   + "WHERE YEAR(t.data_pagamento) = ? AND ty.nome = 'Despesa' "
                   + "GROUP BY d.descricao, mes "
                   + "ORDER BY d.descricao ASC, mes";

        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, anoSelecionado);
            rs = pst.executeQuery();
            Map<String, double[]> despesasMensais = new HashMap<>();
            double[] totalDespesasPorMes = new double[12];

            while (rs.next()) {
                String descricao = rs.getString("descricao");
                double valor = rs.getDouble("total");
                int mes = rs.getInt("mes") - 1;

                despesasMensais.computeIfAbsent(descricao, k -> new double[12])[mes] += valor;
                totalDespesasPorMes[mes] += valor;
            }

            for (Map.Entry<String, double[]> entry : despesasMensais.entrySet()) {
                adicionarLinhaTabelaComTotal(model, entry.getKey(), entry.getValue());
            }

            // Calcule o total anual e adicione à tabela
            double totalAnualDespesas = calcularTotalAnual(totalDespesasPorMes);
            adicionarLinhaTabelaTotalAnual(model, "Total de Despesas", totalDespesasPorMes, totalAnualDespesas);
            ajustarLarguraColunas(tblSaidas);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar as saídas: " + e.getMessage());
        } finally {
            fecharRecursos();
        }
    }




// Método auxiliar para calcular o total anual
private double calcularTotalAnual(double[] valoresMensais) {
    double totalAnual = 0.0;
    for (double valor : valoresMensais) {
        totalAnual += valor;
    }
    return totalAnual;
}

// Método auxiliar para formatar valores
private String formatarValor(double valor) {
    return valor == 0 ? "" : String.format("%.2f", valor).replace(".", ",");
}

// Método auxiliar para adicionar linhas de forma horizontal (com valores zero removidos)
private void adicionarLinhaTabelaHorizontal(DefaultTableModel model, String categoria, double[] valoresMensais, double totalAnual) {
    Object[] row = new Object[14]; 
    row[0] = categoria;
    for (int i = 0; i < 12; i++) {
        row[i + 1] = formatarValor(valoresMensais[i]);
    }
    row[13] = formatarValor(totalAnual);
    model.addRow(row);
}

// Método auxiliar para adicionar total anual
private void adicionarLinhaTabelaTotalAnual(DefaultTableModel model, String descricao, double[] totaisMensais, double totalAnual) {
    Object[] row = new Object[14]; 
    row[0] = descricao;
    for (int i = 0; i < 12; i++) {
        row[i + 1] = formatarValor(totaisMensais[i]);
    }
    row[13] = formatarValor(totalAnual);
    model.addRow(row);
}

private void ajustarLarguraColunas(JTable table) {
    for (int i = 0; i < table.getColumnCount(); i++) {
        TableColumn column = table.getColumnModel().getColumn(i);
        int width = 0;

        for (int j = 0; j < table.getRowCount(); j++) {
            TableCellRenderer renderer = table.getCellRenderer(j, i);
            Component comp = table.prepareRenderer(renderer, j, i);
            width = Math.max(width, comp.getPreferredSize().width);
        }
        column.setPreferredWidth(width + 10);
    }
}


private void fecharRecursos() {
    try {
        if (rs != null) rs.close();
        if (pst != null) pst.close();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Erro ao fechar recursos: " + e.getMessage());
    }
}








    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbAnos = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblEntradas = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSaidas = new javax.swing.JTable();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("Controle fluxo de caixa");

        tblEntradas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descrição", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro", "Total Anual"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblEntradas);

        tblSaidas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Descrição", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro", "Total Anual"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(tblSaidas);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1230, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmbAnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmbAnos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        setBounds(0, 0, 1266, 823);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbAnos;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblEntradas;
    private javax.swing.JTable tblSaidas;
    // End of variables declaration//GEN-END:variables
}
