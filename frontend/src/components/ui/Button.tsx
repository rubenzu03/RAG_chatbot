import type { ButtonHTMLAttributes, ReactNode } from 'react';

type ButtonVariant = 'brand' | 'primary' | 'secondary' | 'danger';
type ButtonAlign = 'center' | 'left';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: ButtonVariant;
  fullWidth?: boolean;
  align?: ButtonAlign;
}

const variantClasses: Record<ButtonVariant, string> = {
  brand: 'bg-message-user-dark text-white hover:opacity-90',
  primary: 'bg-blue-600 text-white hover:bg-blue-700',
  secondary: 'bg-gray-600 text-white hover:bg-gray-700',
  danger: 'bg-red-600 text-white hover:bg-red-700',
};

const alignClasses: Record<ButtonAlign, string> = {
  center: 'text-center justify-center',
  left: 'text-left justify-start',
};

export default function Button({
  children,
  variant = 'primary',
  fullWidth = false,
  align = 'center',
  className = '',
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={[
        'rounded-lg px-4 py-2.5 font-semibold transition-colors',
        'disabled:cursor-not-allowed disabled:opacity-50',
        'inline-flex items-center',
        fullWidth ? 'w-full' : '',
        alignClasses[align],
        variantClasses[variant],
        className,
      ]
        .filter(Boolean)
        .join(' ')}
      {...props}
    >
      {children}
    </button>
  );
}
