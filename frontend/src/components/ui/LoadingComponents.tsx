import React from 'react';

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  message?: string;
  className?: string;
}

const sizeClasses = {
  sm: 'h-6 w-6',
  md: 'h-12 w-12',
  lg: 'h-32 w-32'
};

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ 
  size = 'md', 
  message = 'Loading...', 
  className = '' 
}) => {
  return (
    <div className={`flex flex-col items-center justify-center p-4 ${className}`}>
      <div className={`animate-spin rounded-full border-b-2 border-green-500 ${sizeClasses[size]} mb-2`}></div>
      {message && <p className="text-white text-sm">{message}</p>}
    </div>
  );
};

export interface ErrorDisplayProps {
  error: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorDisplay: React.FC<ErrorDisplayProps> = ({ 
  error, 
  onRetry, 
  className = '' 
}) => {
  return (
    <div className={`flex flex-col items-center justify-center p-6 text-center ${className}`}>
      <div className="text-red-500 text-4xl mb-4">⚠️</div>
      <h3 className="text-white text-lg mb-2">Error</h3>
      <p className="text-red-400 mb-4 max-w-md">{error}</p>
      {onRetry && (
        <button 
          onClick={onRetry}
          className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg transition-colors"
        >
          Retry
        </button>
      )}
    </div>
  );
};

export interface DataCardProps {
  title: string;
  loading?: boolean;
  error?: string;
  onRetry?: () => void;
  children: React.ReactNode;
  className?: string;
}

export const DataCard: React.FC<DataCardProps> = ({
  title,
  loading,
  error,
  onRetry,
  children,
  className = ''
}) => {
  return (
    <div className={`bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur ${className}`}>
      <h2 className="text-2xl font-bold text-white mb-4">{title}</h2>
      
      {loading && (
        <LoadingSpinner size="sm" message="Loading data..." />
      )}
      
      {error && !loading && (
        <ErrorDisplay error={error} onRetry={onRetry} />
      )}
      
      {!loading && !error && children}
    </div>
  );
};

export default LoadingSpinner;