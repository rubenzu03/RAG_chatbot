import type { ReactNode } from 'react';

type AlertVariant = 'error' | 'success' | 'info';

interface AlertProps {
  children: ReactNode;
  variant?: AlertVariant;
  className?: string;
}

const variantClasses: Record<AlertVariant, string> = {
  error: 'bg-red-500/20 text-red-300',
  success: 'bg-green-500/20 text-green-300',
  info: 'bg-blue-500/20 text-blue-200',
};

export default function Alert({ children, variant = 'info', className = '' }: AlertProps) {
  const alertType = variant === 'error' ? 'alert' : 'status';
  const ariaLive = variant === 'error' ? 'assertive' : 'polite';
  return (
    <div role={alertType} aria-live={ariaLive} className={['rounded-lg p-3 text-sm', variantClasses[variant], className].join(' ')}>
      {children}
    </div>
  );
}
