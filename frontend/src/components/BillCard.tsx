type Props = {
  title: string;
  basePrintNoStr: string;
  summary?: string;
  chamber?: string;
  onOpen: (basePrintNoStr: string) => void;
};

export default function BillCard({ title, basePrintNoStr, summary, chamber, onOpen }: Props) {
  return (
    <div className="border border-gray-700 rounded-lg p-6 hover:shadow-lg transition-shadow bg-gray-900">
      <div className="flex items-center justify-between mb-3">
        <span className="inline-block bg-blue-600 text-white text-xs font-semibold px-2.5 py-1 rounded">
          {basePrintNoStr}
        </span>
        {chamber && (
          <span className={`text-xs px-2 py-1 rounded font-medium ${
            chamber === 'SENATE' 
              ? 'bg-green-900 text-green-300' 
              : 'bg-purple-900 text-purple-300'
          }`}>
            {chamber === 'SENATE' ? 'Senate' : 'Assembly'}
          </span>
        )}
      </div>

      <h3 className="font-bold text-white text-lg leading-tight mb-3">{title}</h3>
      
      {summary && (
        <p className="text-gray-400 text-sm leading-relaxed mb-4 line-clamp-2">
          {summary}
        </p>
      )}
      
      <div className="flex items-center justify-between pt-3 border-t border-gray-700">
        <button 
          onClick={() => onOpen(basePrintNoStr)} 
          className="text-blue-400 hover:text-blue-300 font-medium text-sm transition-colors"
        >
          View Details →
        </button>
        <button 
          className="bg-gray-800 text-gray-400 text-sm px-4 py-2 rounded-md font-medium cursor-not-allowed opacity-50"
          disabled
          title="Save requires sign-in"
        >
            Save
          </button>
      </div>
    </div>
  );
}


