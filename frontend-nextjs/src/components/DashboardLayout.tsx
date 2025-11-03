'use client';

import React, { useState } from 'react';
import { Sidebar } from './Sidebar';
import { DashboardHeader } from './DashboardHeader';
import styles from './DashboardLayout.module.css';

interface NavItem {
  href: string;
  icon: string;
  label: string;
}

interface DashboardLayoutProps {
  children: React.ReactNode;
  navItems: NavItem[];
  userName?: string;
  userRole?: string;
  onLogout?: () => void;
  showSearch?: boolean;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  children,
  navItems,
  userName,
  userRole,
  onLogout,
  showSearch = true
}) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => setSidebarOpen(!sidebarOpen);
  const closeSidebar = () => setSidebarOpen(false);

  return (
    <div className={styles.dashboardLayout}>
      <Sidebar navItems={navItems} isOpen={sidebarOpen} onClose={closeSidebar} />

      <div className={styles.mainContent}>
        <DashboardHeader
          userName={userName}
          userRole={userRole}
          onLogout={onLogout}
          onMenuToggle={toggleSidebar}
          showSearch={showSearch}
        />

        <div className={styles.contentArea}>{children}</div>
      </div>
    </div>
  );
};
