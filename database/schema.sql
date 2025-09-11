-- ==========================================
-- Schema de Banco de Dados para Flow Service
-- com Deleção em Cascade Completa
-- ==========================================

-- Extensões necessárias (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. TABELA PRINCIPAL: PROCESSOS
-- ==========================================

CREATE TABLE processes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key VARCHAR(255) UNIQUE NOT NULL, -- Identificador único sanitizado
    name VARCHAR(500) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID, -- Referência ao usuário criador
    
    -- Índices para performance
    CONSTRAINT processes_key_format CHECK (key ~ '^[a-z0-9_-]+$'),
    CONSTRAINT processes_name_length CHECK (LENGTH(name) BETWEEN 1 AND 500)
);

CREATE INDEX idx_processes_key ON processes(key);
CREATE INDEX idx_processes_active ON processes(active);
CREATE INDEX idx_processes_created_at ON processes(created_at);

-- ==========================================
-- 2. ESTÁGIOS DO PROCESSO
-- ==========================================

CREATE TABLE process_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    sla_days INTEGER DEFAULT 0,
    color VARCHAR(7) DEFAULT '#3B82F6', -- Hex color
    display_order INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Se processo for deletado, todos os estágios são removidos
    CONSTRAINT fk_stage_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT stage_color_format CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT stage_sla_positive CHECK (sla_days >= 0)
);

CREATE INDEX idx_stages_process_id ON process_stages(process_id);
CREATE INDEX idx_stages_order ON process_stages(process_id, display_order);

-- ==========================================
-- 3. FORMULÁRIOS DOS ESTÁGIOS
-- ==========================================

CREATE TABLE stage_forms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    stage_id UUID NOT NULL,
    field_id VARCHAR(255) NOT NULL, -- ID do campo no frontend
    field_label VARCHAR(255) NOT NULL,
    field_type VARCHAR(100) NOT NULL, -- text, email, number, select, etc.
    field_required BOOLEAN DEFAULT false,
    field_placeholder TEXT,
    field_options JSONB, -- Para campos select, checkbox, etc.
    field_validation JSONB, -- Regras de validação
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Se estágio for deletado, formulários são removidos
    CONSTRAINT fk_form_stage 
        FOREIGN KEY (stage_id) 
        REFERENCES process_stages(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    UNIQUE(stage_id, field_id)
);

CREATE INDEX idx_forms_stage_id ON stage_forms(stage_id);
CREATE INDEX idx_forms_order ON stage_forms(stage_id, display_order);

-- ==========================================
-- 4. PROPOSTAS/REGISTROS DO PROCESSO
-- ==========================================

CREATE TABLE process_proposals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    current_stage_id UUID,
    name VARCHAR(500) NOT NULL,
    amount DECIMAL(15,2) DEFAULT 0,
    status VARCHAR(100) DEFAULT 'Pendente',
    is_archived BOOLEAN DEFAULT false,
    stage_entered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    
    -- CASCADE: Se processo for deletado, propostas são removidas
    CONSTRAINT fk_proposal_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- Estágio atual pode ser NULL (proposta não iniciada)
    CONSTRAINT fk_proposal_stage 
        FOREIGN KEY (current_stage_id) 
        REFERENCES process_stages(id) 
        ON DELETE SET NULL ON UPDATE CASCADE,
        
    CONSTRAINT proposal_amount_positive CHECK (amount >= 0)
);

CREATE INDEX idx_proposals_process_id ON process_proposals(process_id);
CREATE INDEX idx_proposals_stage_id ON process_proposals(current_stage_id);
CREATE INDEX idx_proposals_status ON process_proposals(status);
CREATE INDEX idx_proposals_archived ON process_proposals(is_archived);

-- ==========================================
-- 5. DADOS DOS FORMULÁRIOS PREENCHIDOS
-- ==========================================

CREATE TABLE proposal_form_data (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID NOT NULL,
    stage_id UUID NOT NULL,
    field_id VARCHAR(255) NOT NULL,
    field_value JSONB, -- Valor pode ser string, number, array, object
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Se proposta for deletada, dados do formulário são removidos
    CONSTRAINT fk_form_data_proposal 
        FOREIGN KEY (proposal_id) 
        REFERENCES process_proposals(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- CASCADE: Se estágio for deletado, dados são removidos
    CONSTRAINT fk_form_data_stage 
        FOREIGN KEY (stage_id) 
        REFERENCES process_stages(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    UNIQUE(proposal_id, stage_id, field_id)
);

CREATE INDEX idx_form_data_proposal_id ON proposal_form_data(proposal_id);
CREATE INDEX idx_form_data_stage_id ON proposal_form_data(stage_id);

-- ==========================================
-- 6. ARQUIVOS/ANEXOS
-- ==========================================

CREATE TABLE process_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID,
    proposal_id UUID,
    stage_id UUID,
    original_name VARCHAR(500) NOT NULL,
    stored_name VARCHAR(500) NOT NULL, -- Nome no storage
    file_path TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(255),
    file_hash VARCHAR(64), -- SHA-256 para deduplicação
    uploaded_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Arquivos são removidos quando processo é deletado
    CONSTRAINT fk_file_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- CASCADE: Arquivos são removidos quando proposta é deletada
    CONSTRAINT fk_file_proposal 
        FOREIGN KEY (proposal_id) 
        REFERENCES process_proposals(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- CASCADE: Arquivos são removidos quando estágio é deletado
    CONSTRAINT fk_file_stage 
        FOREIGN KEY (stage_id) 
        REFERENCES process_stages(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    CONSTRAINT file_size_positive CHECK (file_size > 0),
    -- Pelo menos um dos IDs deve estar presente
    CONSTRAINT file_reference_required 
        CHECK (process_id IS NOT NULL OR proposal_id IS NOT NULL OR stage_id IS NOT NULL)
);

CREATE INDEX idx_files_process_id ON process_files(process_id);
CREATE INDEX idx_files_proposal_id ON process_files(proposal_id);
CREATE INDEX idx_files_hash ON process_files(file_hash);

-- ==========================================
-- 7. HISTÓRICO DE ATIVIDADES
-- ==========================================

CREATE TABLE process_activity_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    proposal_id UUID,
    stage_id UUID,
    action VARCHAR(255) NOT NULL, -- 'created', 'updated', 'moved_stage', etc.
    description TEXT,
    metadata JSONB, -- Dados adicionais da ação
    user_id UUID,
    user_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Log é removido quando processo é deletado
    CONSTRAINT fk_log_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- CASCADE: Log é removido quando proposta é deletada
    CONSTRAINT fk_log_proposal 
        FOREIGN KEY (proposal_id) 
        REFERENCES process_proposals(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    -- SET NULL: Se estágio for deletado, manter log mas limpar referência
    CONSTRAINT fk_log_stage 
        FOREIGN KEY (stage_id) 
        REFERENCES process_stages(id) 
        ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_log_process_id ON process_activity_log(process_id);
CREATE INDEX idx_log_proposal_id ON process_activity_log(proposal_id);
CREATE INDEX idx_log_created_at ON process_activity_log(created_at);

-- ==========================================
-- 8. CONFIGURAÇÕES DO PROCESSO
-- ==========================================

CREATE TABLE process_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    setting_key VARCHAR(255) NOT NULL,
    setting_value JSONB,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Configurações são removidas quando processo é deletado
    CONSTRAINT fk_setting_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
        
    UNIQUE(process_id, setting_key)
);

CREATE INDEX idx_settings_process_id ON process_settings(process_id);

-- ==========================================
-- 9. ANALYTICS E MÉTRICAS
-- ==========================================

CREATE TABLE process_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_value JSONB NOT NULL,
    aggregation_period VARCHAR(50), -- 'daily', 'weekly', 'monthly'
    period_start TIMESTAMP WITH TIME ZONE,
    period_end TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- CASCADE: Analytics são removidas quando processo é deletado
    CONSTRAINT fk_analytics_process 
        FOREIGN KEY (process_id) 
        REFERENCES processes(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_analytics_process_id ON process_analytics(process_id);
CREATE INDEX idx_analytics_period ON process_analytics(period_start, period_end);

-- ==========================================
-- 10. TRIGGERS PARA UPDATED_AT
-- ==========================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Aplicar trigger em todas as tabelas relevantes
CREATE TRIGGER update_processes_updated_at 
    BEFORE UPDATE ON processes 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_stages_updated_at 
    BEFORE UPDATE ON process_stages 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_proposals_updated_at 
    BEFORE UPDATE ON process_proposals 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_form_data_updated_at 
    BEFORE UPDATE ON proposal_form_data 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_settings_updated_at 
    BEFORE UPDATE ON process_settings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ==========================================
-- 11. FUNÇÃO PARA DELEÇÃO COMPLETA COM LOG
-- ==========================================

CREATE OR REPLACE FUNCTION delete_process_cascade(process_key_param VARCHAR)
RETURNS TABLE (
    deleted_tables TEXT,
    deleted_count BIGINT,
    backup_data JSONB
) AS $$
DECLARE
    process_uuid UUID;
    total_deleted BIGINT := 0;
    table_counts JSONB := '{}';
    full_backup JSONB := '{}';
    temp_count BIGINT;
BEGIN
    -- Buscar o processo
    SELECT id INTO process_uuid FROM processes WHERE key = process_key_param;
    
    IF process_uuid IS NULL THEN
        RAISE EXCEPTION 'Processo com key "%" não encontrado', process_key_param;
    END IF;
    
    -- Criar backup completo antes da deleção
    -- Backup do processo principal
    SELECT to_jsonb(p.*) INTO full_backup FROM processes p WHERE p.id = process_uuid;
    
    -- Backup dos dados relacionados
    full_backup := full_backup || jsonb_build_object(
        'stages', (SELECT jsonb_agg(to_jsonb(s.*)) FROM process_stages s WHERE s.process_id = process_uuid),
        'forms', (SELECT jsonb_agg(to_jsonb(f.*)) FROM stage_forms f 
                   INNER JOIN process_stages s ON f.stage_id = s.id 
                   WHERE s.process_id = process_uuid),
        'proposals', (SELECT jsonb_agg(to_jsonb(p.*)) FROM process_proposals p WHERE p.process_id = process_uuid),
        'form_data', (SELECT jsonb_agg(to_jsonb(fd.*)) FROM proposal_form_data fd 
                      INNER JOIN process_proposals p ON fd.proposal_id = p.id 
                      WHERE p.process_id = process_uuid),
        'files', (SELECT jsonb_agg(to_jsonb(f.*)) FROM process_files f WHERE f.process_id = process_uuid),
        'activity_log', (SELECT jsonb_agg(to_jsonb(al.*)) FROM process_activity_log al WHERE al.process_id = process_uuid),
        'settings', (SELECT jsonb_agg(to_jsonb(s.*)) FROM process_settings s WHERE s.process_id = process_uuid),
        'analytics', (SELECT jsonb_agg(to_jsonb(a.*)) FROM process_analytics a WHERE a.process_id = process_uuid)
    );
    
    -- Contar registros antes da deleção para relatório
    SELECT COUNT(*) INTO temp_count FROM process_analytics WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_analytics', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM process_settings WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_settings', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM process_activity_log WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_activity_log', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM process_files WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_files', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM proposal_form_data fd 
           INNER JOIN process_proposals p ON fd.proposal_id = p.id 
           WHERE p.process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('proposal_form_data', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM process_proposals WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_proposals', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM stage_forms f 
           INNER JOIN process_stages s ON f.stage_id = s.id 
           WHERE s.process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('stage_forms', temp_count);
    total_deleted := total_deleted + temp_count;
    
    SELECT COUNT(*) INTO temp_count FROM process_stages WHERE process_id = process_uuid;
    table_counts := table_counts || jsonb_build_object('process_stages', temp_count);
    total_deleted := total_deleted + temp_count;
    
    -- Deletar o processo (CASCADE fará o resto)
    DELETE FROM processes WHERE id = process_uuid;
    total_deleted := total_deleted + 1;
    table_counts := table_counts || jsonb_build_object('processes', 1);
    
    -- Retornar resultado
    RETURN QUERY SELECT 
        table_counts::TEXT as deleted_tables,
        total_deleted as deleted_count,
        full_backup as backup_data;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- 12. COMENTÁRIOS PARA DOCUMENTAÇÃO
-- ==========================================

COMMENT ON TABLE processes IS 'Tabela principal dos processos/esteiras. Deleção aqui remove TUDO relacionado via CASCADE.';
COMMENT ON TABLE process_stages IS 'Estágios de cada processo. Removidos automaticamente quando processo é deletado.';
COMMENT ON TABLE stage_forms IS 'Definições de formulários por estágio. Removidos quando estágio ou processo é deletado.';
COMMENT ON TABLE process_proposals IS 'Propostas/registros em cada processo. Removidas quando processo é deletado.';
COMMENT ON TABLE proposal_form_data IS 'Dados preenchidos nos formulários. Removidos quando proposta, estágio ou processo é deletado.';
COMMENT ON TABLE process_files IS 'Arquivos anexados. Removidos quando qualquer entidade pai é deletada.';
COMMENT ON TABLE process_activity_log IS 'Log de atividades. Removido quando processo é deletado.';
COMMENT ON TABLE process_settings IS 'Configurações específicas do processo. Removidas quando processo é deletado.';
COMMENT ON TABLE process_analytics IS 'Dados de analytics. Removidos quando processo é deletado.';

COMMENT ON FUNCTION delete_process_cascade IS 'Função para deletar processo com cascade completo e backup automático.';