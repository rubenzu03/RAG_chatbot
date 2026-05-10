import { useState } from 'react';
import Modal from './components/ui/Modal';
import Button from './components/ui/Button';

const PRIVACY_DISCLOSURE_KEY = 'hasSeenDataPrivacyDisclosure';

interface DataPrivacyDisclosureProps {
  onAccept: () => void;
}

function initializeDisclosureState(): boolean {
  const hasSeenDisclosure = localStorage.getItem(PRIVACY_DISCLOSURE_KEY);
  return !hasSeenDisclosure;
}

export default function DataPrivacyDisclosure({ onAccept }: DataPrivacyDisclosureProps) {
  const [isOpen, setIsOpen] = useState(initializeDisclosureState);

  const handleAccept = () => {
    localStorage.setItem(PRIVACY_DISCLOSURE_KEY, 'true');
    setIsOpen(false);
    onAccept();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => {}}
      title="Data Privacy & Security Notice"
      closeOnBackdropClick={false}
      footer={
        <div className="flex gap-3">
          <Button variant="brand" fullWidth onClick={handleAccept}>
            I Understand & Accept
          </Button>
        </div>
      }
    >
      <div className="space-y-4 text-gray-300">
        <div>
          <h3 className="text-sm font-semibold text-gray-200 mb-2">
            Encrypted Conversation Storage
          </h3>
          <p className="text-sm leading-relaxed">
            The conversation history you create with the AI chatbot is securely stored in an
            encrypted format.
          </p>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-gray-200 mb-2">
            Legitimate Use of Stored Data
          </h3>
          <p className="text-sm leading-relaxed">
            Your conversation history is exclusively used for:
          </p>
          <ul className="text-sm mt-2 space-y-1 ml-4 list-disc">
            <li>Providing context to the AI model for improved responses</li>
            <li>Enhancing the quality and relevance of your learning experience</li>
            <li>Allow you to review and continue previous conversations</li>
          </ul>
        </div>

        <div>
          <h3 className="text-sm font-semibold text-gray-200 mb-2">Data Usage & Privacy</h3>
          <p className="text-sm leading-relaxed">
            Data is not used for training the model or any other purposes.
          </p>
        </div>

        <div className="bg-blue-900 bg-opacity-30 border border-blue-700 rounded-lg p-3">
          <p className="text-xs text-gray-300">
            By clicking <strong>"I Understand & Accept"</strong>, you acknowledge that you have read
            and understood this notice regarding encrypted storage and legitimate data usage.
          </p>
        </div>
      </div>
    </Modal>
  );
}
