'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import styles from './Sidebar.module.css';

interface NavItem {
  href: string;
  icon: string;
  label: string;
}

interface SidebarProps {
  navItems: NavItem[];
  isOpen?: boolean;
  onClose?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ navItems, isOpen = false, onClose }) => {
  const pathname = usePathname();

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && <div className={styles.sidebarOverlay} onClick={onClose} />}

      <aside className={`${styles.sidebar} ${isOpen ? styles.active : ''}`}>
        <div className={styles.sidebarHeader}>
          <div className={styles.sidebarLogo}>
            <i className="fas fa-gavel"></i>
            <span className={styles.logoText}>
              Legal<span className={styles.highlight}>ERP</span>
            </span>
          </div>
          <button className={styles.sidebarClose} onClick={onClose} aria-label="Fechar menu">
            <i className="fas fa-times"></i>
          </button>
        </div>

        <nav className={styles.sidebarNav}>
          {navItems.map((item) => {
            const isActive = pathname === item.href || pathname?.startsWith(item.href + '/');
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`${styles.navItem} ${isActive ? styles.active : ''}`}
                onClick={onClose}
              >
                <i className={item.icon}></i>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className={styles.sidebarFooter}>
          <div className={styles.version}>v2.0.1</div>
        </div>
      </aside>
    </>
  );
};
