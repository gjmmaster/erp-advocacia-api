import React from 'react';
import styles from './Input.module.css';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  icon?: React.ReactNode;
  suffix?: string;
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, icon, suffix, error, className = '', ...props }, ref) => {
    return (
      <div className={styles.formGroup}>
        <div className={styles.inputWrapper}>
          {icon && <span className={styles.inputIcon}>{icon}</span>}
          <input
            ref={ref}
            className={`${styles.formControl} ${icon ? styles['formControl--with-icon'] : ''} ${
              suffix ? styles['formControl--with-suffix'] : ''
            } ${error ? styles['formControl--error'] : ''} ${className}`}
            placeholder=" "
            {...props}
          />
          {label && (
            <label className={styles.formLabel} htmlFor={props.id}>
              {label}
            </label>
          )}
          {suffix && <span className={styles.inputSuffix}>{suffix}</span>}
        </div>
        {error && <span className={styles.errorMessage}>{error}</span>}
      </div>
    );
  }
);

Input.displayName = 'Input';
