import type { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
  closeOnBackdropClick?: boolean;
}

export default function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  closeOnBackdropClick = false,
}: ModalProps) {
  if (!isOpen) return null;

  const handleBackdropClick = () => {
    if (closeOnBackdropClick) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
      onClick={handleBackdropClick}
      role="presentation"
    >
      <div
        className="w-full max-w-md rounded-2xl border border-gray-700 bg-[#2f2f2f] shadow-lg max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        {title && (
          <div className="border-b border-gray-700 px-6 py-4">
            <h2 id="modal-title" className="text-xl font-semibold text-gray-200">
              {title}
            </h2>
          </div>
        )}

        <div className="px-6 py-4">{children}</div>

        {footer && <div className="border-t border-gray-700 px-6 py-4">{footer}</div>}
      </div>
    </div>
  );
}
