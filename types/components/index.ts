// Tipos relacionados aos componentes
export interface BaseComponent {
  id?: string
  className?: string
  testId?: string
}

export interface ButtonProps extends BaseComponent {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  onClick?: () => void
}

export interface InputProps extends BaseComponent {
  type?: 'text' | 'email' | 'password' | 'number'
  placeholder?: string
  value?: string
  disabled?: boolean
  required?: boolean
  error?: string
  onChange?: (value: string) => void
}

export interface ModalProps extends BaseComponent {
  isOpen: boolean
  title?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  onClose: () => void
}