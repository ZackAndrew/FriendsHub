import { useEffect, useState } from 'react';
import { availabilityApi } from '../api/availability';
import type { CommonSlot } from '../types';
import toast from 'react-hot-toast';
import { format, addDays } from 'date-fns';
import { Clock, Users, CalendarDays, RefreshCw } from 'lucide-react';

export default function CommonSlots() {
  const [slots, setSlots] = useState<CommonSlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [fromDate, setFromDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [toDate, setToDate] = useState(format(addDays(new Date(), 7), 'yyyy-MM-dd'));

  const loadSlots = async () => {
    setLoading(true);
    try {
      const res = await availabilityApi.getCommonSlots(fromDate, toDate);
      setSlots(res.data);
    } catch (err) {
      toast.error('Failed to load common slots');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots();
  }, []);

  const handleRefresh = () => {
    loadSlots();
  };

  const groupedSlots = slots.reduce(
    (acc, slot) => {
      if (!acc[slot.friendUsername]) {
        acc[slot.friendUsername] = [];
      }
      acc[slot.friendUsername].push(slot);
      return acc;
    },
    {} as Record<string, CommonSlot[]>
  );

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <h1 className="text-xl md:text-2xl font-bold text-slate-900">Common Free Time</h1>
        <button
          onClick={handleRefresh}
          disabled={loading}
          className="flex items-center justify-center gap-2 px-4 py-2.5 bg-slate-200 text-slate-700 rounded-lg hover:bg-slate-300 transition-colors disabled:opacity-50 text-sm md:text-base"
        >
          <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
          Refresh
        </button>
      </div>

      {/* Date Range Filter */}
      <div className="bg-white rounded-xl border border-slate-200 p-3 md:p-4">
        <div className="flex flex-col sm:flex-row items-stretch sm:items-end gap-3">
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">From</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
            />
          </div>
          <div className="flex-1">
            <label className="block text-sm font-medium text-slate-700 mb-1">To</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm"
            />
          </div>
          <button
            onClick={handleRefresh}
            className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm"
          >
            Search
          </button>
        </div>
      </div>

      {/* Slots */}
      {loading ? (
        <div className="flex items-center justify-center h-48">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      ) : Object.keys(groupedSlots).length === 0 ? (
        <div className="text-center py-8 md:py-12 bg-white rounded-xl border border-slate-200">
          <Users size={40} className="mx-auto text-slate-300 mb-3 md:w-12 md:h-12" />
          <p className="text-sm md:text-base text-slate-500">No common free time found</p>
          <p className="text-xs md:text-sm text-slate-400 mt-1">
            Make sure you and your friends have added availability slots
          </p>
        </div>
      ) : (
        <div className="space-y-4 md:space-y-6">
          {Object.entries(groupedSlots).map(([friendUsername, friendSlots]) => (
            <div key={friendUsername} className="bg-white rounded-xl border border-slate-200 overflow-hidden">
              <div className="bg-slate-50 px-4 md:px-6 py-3 md:py-4 border-b border-slate-200 flex items-center gap-3">
                <div className="w-8 h-8 md:w-10 md:h-10 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-medium text-sm md:text-base">
                  {friendUsername[0].toUpperCase()}
                </div>
                <div>
                  <p className="font-semibold text-sm md:text-base text-slate-900">{friendUsername}</p>
                  <p className="text-xs text-slate-400">
                    {friendSlots.length} common slot{friendSlots.length > 1 ? 's' : ''}
                  </p>
                </div>
              </div>
              <div className="p-3 md:p-4 space-y-2">
                {friendSlots.map((slot, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 p-2.5 md:p-3 bg-emerald-50 rounded-lg"
                  >
                    <CalendarDays size={16} className="text-emerald-600 shrink-0 md:w-[18px] md:h-[18px]" />
                    <div className="flex-1 min-w-0">
                      <p className="text-xs md:text-sm font-medium text-slate-700">
                        {format(new Date(slot.startTime), 'EEEE, MMM d')}
                      </p>
                      <p className="text-xs text-slate-500">
                        {format(new Date(slot.startTime), 'HH:mm')} –{' '}
                        {format(new Date(slot.endTime), 'HH:mm')}
                      </p>
                    </div>
                    <Clock size={12} className="text-emerald-400 shrink-0 md:w-3.5 md:h-3.5" />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
