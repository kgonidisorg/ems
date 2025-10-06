'use client';

import React, { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { LoginForm } from '@/components/auth/LoginForm';
import { RegisterForm } from '@/components/auth/RegisterForm';
import { useAuth } from '@/contexts/AuthContext';
import Image from 'next/image';

function LoginPageContent() {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  
  // Get the return URL from query params, default to home
  const returnUrl = searchParams.get('returnUrl') || '/';

  useEffect(() => {
    // If user is already authenticated, redirect to return URL
    if (!isLoading && isAuthenticated) {
      router.push(returnUrl);
    }
  }, [isAuthenticated, isLoading, router, returnUrl]);

  const handleSuccessfulAuth = () => {
    // Redirect to the return URL after successful authentication
    router.push(returnUrl);
  };

  // Show loading while checking auth status
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto mb-4"></div>
          <p className="text-white">Loading...</p>
        </div>
      </div>
    );
  }

  // Don't render login form if already authenticated (will redirect)
  if (isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800">
      {/* Header Banner */}
      <div className="w-full bg-slate-900 shadow-lg">
        <Image
          src="/banner.jpg"
          alt="EcoGrid Banner"
          className="w-full h-32 object-cover object-bottom"
          width={1200}
          height={128}
        />
        <div className="bg-slate-800 text-white py-3 shadow-md px-6 text-2xl">
          <div className="flex items-center space-x-3">
            <Image
              src="/green-energy-icon.svg"
              alt="EcoGrid Logo"
              className="h-8 w-8"
              width={32}
              height={32}
            />
            <span className="font-bold text-primary">EcoGrid</span>
          </div>
        </div>
      </div>

      {/* Login Form Container */}
      <div className="flex items-center justify-center px-4 py-12">
        <div className="max-w-md w-full">
          <div className="bg-slate-800/80 backdrop-blur rounded-xl shadow-2xl border border-green-500/20 p-8">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-bold text-white mb-2">
                {mode === 'login' ? 'Welcome Back' : 'Join EcoGrid'}
              </h1>
              <p className="text-slate-400">
                {mode === 'login' 
                  ? 'Sign in to access the Energy Management System'
                  : 'Create your account to get started'
                }
              </p>
            </div>

            {mode === 'login' ? (
              <LoginForm
                onSuccess={handleSuccessfulAuth}
                onSwitchToRegister={() => setMode('register')}
              />
            ) : (
              <RegisterForm
                onSuccess={handleSuccessfulAuth}
                onSwitchToLogin={() => setMode('login')}
              />
            )}

            {/* Return URL Info */}
            {returnUrl !== '/' && (
              <div className="mt-6 p-3 bg-blue-500/10 border border-blue-500/20 rounded-lg">
                <p className="text-blue-400 text-sm text-center">
                  You will be redirected to your requested page after signing in
                </p>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="text-center mt-8">
            <p className="text-slate-500 text-sm">
              © 2025 EcoGrid Energy Management System
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto mb-4"></div>
          <p className="text-white">Loading...</p>
        </div>
      </div>
    }>
      <LoginPageContent />
    </Suspense>
  );
}