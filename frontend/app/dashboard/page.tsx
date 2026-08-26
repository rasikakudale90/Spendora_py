"use client";

import { useState } from "react";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/ui/card";
import { DashboardSkeleton } from "@/components/LoadingSkeleton";
import Hero3D from "@/components/Hero3D";

export default function DashboardPage() {
  return (
    <div className="space-y-8">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            Financial Dashboard
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Real-time spending analysis and monthly budget control
          </p>
        </div>
      </div>
      
      {/* 3D Visual Moment */}
      <Hero3D />
    </div>
  );
}
