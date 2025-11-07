'use client';

import styles from './HistoricoTimeline.module.css';

interface HistoricoItem {
  id: string;
  acao: string;
  campo_alterado?: string;
  valor_anterior?: string;
  valor_novo?: string;
  user_name: string;
  created_at: string;
}

interface Props {
  items: HistoricoItem[];
}

export default function HistoricoTimeline({ items }: Props) {
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'agora mesmo';
    if (diffMins < 60) return `há ${diffMins} minuto${diffMins > 1 ? 's' : ''}`;
    if (diffHours < 24) return `há ${diffHours} hora${diffHours > 1 ? 's' : ''}`;
    if (diffDays < 7) return `há ${diffDays} dia${diffDays > 1 ? 's' : ''}`;

    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getActionIcon = (acao: string): string => {
    if (acao.includes('criou') || acao.includes('criado')) return '✨';
    if (acao.includes('atualizou') || acao.includes('alterou')) return '✏️';
    if (acao.includes('deletou') || acao.includes('removeu')) return '🗑️';
    if (acao.includes('status')) return '🔄';
    return '📝';
  };

  const getActionColor = (acao: string): string => {
    if (acao.includes('criou') || acao.includes('criado')) return 'create';
    if (acao.includes('atualizou') || acao.includes('alterou')) return 'update';
    if (acao.includes('deletou') || acao.includes('removeu')) return 'delete';
    if (acao.includes('status')) return 'status';
    return 'default';
  };

  if (items.length === 0) {
    return (
      <div className={styles.empty}>
        <p>📋 Nenhuma alteração registrada</p>
      </div>
    );
  }

  return (
    <div className={styles.timeline}>
      {items.map((item, index) => (
        <div key={item.id} className={styles.item}>
          <div className={`${styles.icon} ${styles[getActionColor(item.acao)]}`}>
            {getActionIcon(item.acao)}
          </div>
          
          <div className={styles.content}>
            <div className={styles.header}>
              <span className={styles.user}>{item.user_name}</span>
              <span className={styles.action}>{item.acao}</span>
            </div>

            {item.campo_alterado && (
              <div className={styles.change}>
                <span className={styles.field}>{item.campo_alterado}:</span>
                {item.valor_anterior && (
                  <span className={styles.oldValue}>
                    {item.valor_anterior}
                  </span>
                )}
                <span className={styles.arrow}>→</span>
                <span className={styles.newValue}>
                  {item.valor_novo}
                </span>
              </div>
            )}

            <div className={styles.date}>
              {formatDate(item.created_at)}
            </div>
          </div>

          {index < items.length - 1 && <div className={styles.line}></div>}
        </div>
      ))}
    </div>
  );
}
