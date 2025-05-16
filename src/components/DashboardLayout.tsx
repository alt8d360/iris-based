
import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { LogOut, Home, CreditCard, User, Menu, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { useIsMobile } from '@/hooks/use-mobile';

interface DashboardLayoutProps {
  children: React.ReactNode;
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { toast } = useToast();
  const isMobile = useIsMobile();
  const [isSidebarOpen, setIsSidebarOpen] = useState(!isMobile);
  
  const user = JSON.parse(localStorage.getItem('user') || '{"name": "Demo User"}');

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    
    toast({
      title: "Logged out",
      description: "You have been successfully logged out.",
    });
    
    navigate('/');
  };

  const navigationItems = [
    {
      title: 'Home',
      icon: <Home className="h-5 w-5" />,
      path: '/dashboard',
    },
    {
      title: 'Transactions',
      icon: <CreditCard className="h-5 w-5" />,
      path: '/transactions',
    },
    {
      title: 'Profile',
      icon: <User className="h-5 w-5" />,
      path: '/profile',
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile nav header */}
      <div className="lg:hidden flex items-center justify-between p-4 border-b bg-white">
        <div className="flex items-center">
          <Button 
            variant="ghost" 
            size="icon"
            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
          >
            <Menu className="h-6 w-6" />
          </Button>
          <h1 className="text-xl font-bold text-blue-600 ml-3">IrisPay</h1>
        </div>
        <Button variant="ghost" size="sm" onClick={handleLogout}>
          <LogOut className="h-5 w-5" />
        </Button>
      </div>
      
      {/* Sidebar */}
      <div className={cn(
        "fixed inset-y-0 left-0 z-50 w-64 bg-white border-r transform transition-transform duration-200 ease-in-out",
        isSidebarOpen ? "translate-x-0" : "-translate-x-full",
        "lg:translate-x-0 lg:static"
      )}>
        {/* Sidebar header with close button on mobile */}
        <div className="flex items-center justify-between h-16 px-6 border-b">
          <div className="flex items-center">
            <h1 className="text-xl font-bold text-blue-600">IrisPay</h1>
          </div>
          
          {isMobile && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setIsSidebarOpen(false)}
            >
              <X className="h-5 w-5" />
            </Button>
          )}
        </div>
        
        {/* User info */}
        <div className="p-6 border-b">
          <div className="flex items-center space-x-3">
            <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
              <span className="font-medium text-blue-600">
                {user.name.charAt(0).toUpperCase()}
              </span>
            </div>
            <div>
              <p className="font-medium">{user.name}</p>
              <p className="text-sm text-gray-500">{user.email || 'demo@example.com'}</p>
            </div>
          </div>
        </div>
        
        {/* Navigation */}
        <nav className="p-4 space-y-1">
          {navigationItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              onClick={() => isMobile && setIsSidebarOpen(false)}
              className={cn(
                "flex items-center space-x-3 px-3 py-2 rounded-md transition-colors",
                location.pathname === item.path
                  ? "bg-blue-50 text-blue-700"
                  : "hover:bg-gray-100"
              )}
            >
              {item.icon}
              <span>{item.title}</span>
            </Link>
          ))}
          
          <button 
            onClick={handleLogout}
            className="w-full flex items-center space-x-3 px-3 py-2 rounded-md hover:bg-gray-100 text-left mt-6"
          >
            <LogOut className="h-5 w-5" />
            <span>Log out</span>
          </button>
        </nav>
      </div>
      
      {/* Main content */}
      <div className={cn(
        "flex-1",
        isSidebarOpen ? "lg:ml-64" : ""
      )}>
        {children}
      </div>
      
      {/* Overlay for mobile when sidebar is open */}
      {isMobile && isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}
    </div>
  );
};

export default DashboardLayout;
